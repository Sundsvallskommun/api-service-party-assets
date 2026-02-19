package se.sundsvall.partyassets.pr3import;

import com.fasterxml.jackson.annotation.JsonIgnore;
import generated.se.sundsvall.party.PartyType;
import jakarta.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.party.PartyClient;

import static com.nimbusds.oauth2.sdk.util.StringUtils.isNotBlank;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.zalando.problem.Status.CONFLICT;
import static se.sundsvall.partyassets.integration.db.model.PartyType.PRIVATE;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toEntity;

@Component
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
class PR3Importer {

	static final String DRIVER = "driver";

	static final String PASSENGER = "passenger";

	static final String DRIVER_SHORT = "F";

	static final String PASSENGER_SHORT = "P";

	private static final String PARAM_REGISTRATION_NUMBER = "registrationNumber";

	private static final String PARAM_CARD_PRINTED = "cardPrinted";

	private static final String PARAM_SMART_PARK_SYNC = "smartParkSync";

	private static final String PARAM_ISSUED_BY_ADMINISTRATION = "issuedByAdministration";

	private static final String PARAM_ISSUED_BY_ADMINISTRATOR = "issuedByAdministrator";

	private static final String PARAM_APPLIED_AS = "appliedAs";

	private static final String PARAM_PERMIT_FULL_NUMBER = "permitFullNumber";

	private static final int COL_SEX = 4;

	private static final int COL_APPLIED_AS = 5;

	private static final int COL_ASSET_ID = 7;

	private static final int COL_LEGAL_ID = 10;

	private static final int COL_REGISTRATION_NUMBER = 15;

	private static final int COL_ISSUED_DATE = 16;

	private static final int COL_VALID_TO_DATE = 18;

	private static final int COL_CARD_PRINTED = 21;

	private static final int COL_ISSUED_BY_ADMINISTRATION = 23;

	private static final int COL_ISSUED_BY_ADMINISTRATOR = 24;

	private static final int COL_SMART_PARK_SYNC = 27;

	private static final DateTimeFormatter PERSONAL_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

	private final PR3ImportProperties properties;

	private final AssetRepository assetRepository;

	private final PartyClient partyClient;

	private final Validator validator;

	PR3Importer(final PR3ImportProperties properties, final AssetRepository assetRepository,
		final PartyClient partyClient, final Validator validator) {
		this.properties = properties;
		this.assetRepository = assetRepository;
		this.partyClient = partyClient;
		this.validator = validator;
	}

	static boolean verifyCheckDigit(final String legalId) {
		var sum = 0;
		var alternate = false;

		// Start from the right, moving left
		for (var i = legalId.length() - 1; i >= 0; --i) {
			// Get the current digit
			int digit = Character.getNumericValue(legalId.charAt(i));
			// Double every other digit
			digit = alternate ? (digit * 2) : digit;
			// Subtract 9 if the value is greater than 9 (the same as summing the digits)
			digit = (digit > 9) ? (digit - 9) : digit;
			// Add the digit to the sum
			sum += digit;
			// Flip the alternate flag
			alternate = !alternate;
		}

		return (sum % 10) == 0;
	}

	/**
	 * Imports assets from the Excel file read from the given input stream.
	 *
	 * @param  in          the input stream to read the Excel file from.
	 * @return             the import result.
	 * @throws IOException on any errors.
	 */
	Result importFromExcel(final InputStream in, final String municipalityId) throws IOException {
		final var result = new Result();

		var lastFailedRowIndex = 1;
		final var out = new ByteArrayOutputStream();

		try (final var sourceWorkbook = new ReadableWorkbook(in);
			final var failedEntriesWorkbook = new Workbook(out, "party-assets", null)) {
			final var sourceSheet = sourceWorkbook.getFirstSheet();
			final var failedEntriesSheet = failedEntriesWorkbook.newWorksheet(sourceSheet.getName());

			// Sort the rows on asset id, descending
			final var rows = sourceSheet.read().stream()
				.sorted(comparing(r -> r.getCellText(COL_ASSET_ID)))
				.toList()
				.reversed();

			result.setTotal(rows.size() - 1);

			// Copy the header row
			copyHeaderRow(rows.getFirst(), failedEntriesSheet);

			// Process the rest of the rows
			for (var rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
				final var row = rows.get(rowIndex);

				// Create an asset (create request, to take advantage of validation constraints) and
				// fill in the static info
				final var assetCreateRequest = new AssetCreateRequest()
					.withOrigin(properties.staticAssetInfo().origin())
					.withType(properties.staticAssetInfo().type())
					.withDescription(properties.staticAssetInfo().description());

				// Fill in the rest of the asset information from the current row
				extractAssetId(row).ifPresent(assetCreateRequest::setAssetId);
				final var legalId = extractLegalId(row);

				// Manually check the legal id, since there's a real chance that the input file
				// contains crap legal ids
				if (legalId.isPresent()) {
					// Verify the date part of the legal id
					try {
						PERSONAL_NUMBER_FORMATTER.parse(legalId.get().substring(0, 6));
					} catch (final Exception e) {
						copyRow(row, failedEntriesSheet, lastFailedRowIndex++, of("Invalid legal id"));
						continue;
					}

					// Verify the check digit on the legal id (without century digits)
					if (!verifyCheckDigit(legalId.get())) {
						copyRow(row, failedEntriesSheet, lastFailedRowIndex++, of("Invalid legal id (check digit)"));
						continue;
					}

					// Attempt to get the party id, by trying first "19" and then "20" as century digits
					var partyId = partyClient.getPartyId(municipalityId, PartyType.PRIVATE, "19" + legalId.get());
					if (partyId.isEmpty()) {
						partyId = partyClient.getPartyId(municipalityId, PartyType.PRIVATE, "20" + legalId.get());
					}

					if (partyId.isPresent()) {
						assetCreateRequest.setPartyId(partyId.get());
					} else {
						copyRow(row, failedEntriesSheet, lastFailedRowIndex++, of("Unable to get party id"));
						continue;
					}
				}

				extractIssuedDate(row).ifPresent(assetCreateRequest::setIssued);
				extractValidToDate(row).ifPresent(assetCreateRequest::setValidTo);
				extractStatus(row).ifPresent(assetCreateRequest::setStatus);
				extractRegistrationNumber(row).ifPresent(value -> assetCreateRequest.setAdditionalParameter(PARAM_REGISTRATION_NUMBER, value));
				extractCardPrinted(row).ifPresent(value -> assetCreateRequest.setAdditionalParameter(PARAM_CARD_PRINTED, value.format(DateTimeFormatter.ISO_DATE)));
				extractSmartParkSync(row).ifPresent(value -> assetCreateRequest.setAdditionalParameter(PARAM_SMART_PARK_SYNC, value));
				extractIssuedByAdministration(row).ifPresent(value -> assetCreateRequest.setAdditionalParameter(PARAM_ISSUED_BY_ADMINISTRATION, value));
				extractIssuedByAdministrator(row).ifPresent(value -> assetCreateRequest.setAdditionalParameter(PARAM_ISSUED_BY_ADMINISTRATOR, value));
				extractAppliedAs(row).ifPresent(value -> assetCreateRequest.setAdditionalParameter(PARAM_APPLIED_AS, value));
				// Create the full permit number as {municipality id}-{asset id}-{birth year}{sex}-{applied as}
				extractSex(row).ifPresent(sex -> {
					// Sanity check...
					final var assetId = assetCreateRequest.getAssetId();

					final var appliedAsRaw = Optional.ofNullable(assetCreateRequest.getAdditionalParameters())
						.orElse(emptyMap())
						.get(PARAM_APPLIED_AS);

					final var appliedAs = switch (appliedAsRaw) {
						case null -> null;
						case DRIVER -> DRIVER_SHORT;
						case PASSENGER -> PASSENGER_SHORT;
						default -> null;
					};

					final var birthYear = legalId.map(value -> value.substring(0, 2)).orElse(null);

					if (isNotBlank(assetId) && isNotBlank(birthYear) && isNotBlank(appliedAs)) {
						final var permitFullNumber = String.format("%s-%s-%s%s-%s",
							properties.staticAssetInfo().municipalityId(), assetId, birthYear, sex, appliedAs);

						assetCreateRequest.setAdditionalParameter(PARAM_PERMIT_FULL_NUMBER, permitFullNumber);
					}
				});

				var errorDetail = Optional.<String>empty();

				// Validate the asset
				final var constraintViolations = validator.validate(assetCreateRequest);
				if (constraintViolations.isEmpty()) {
					// Save the asset - reusing the asset service would indeed be a viable option,
					// but as we know when importing PR3 data that we're always storing private assets
					// we won't need to make the extra calls to the party service to determine the
					// actual party type
					try {
						if (assetRepository.existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), municipalityId)) {
							throw Problem.builder()
								.withStatus(CONFLICT)
								.withTitle("Asset already exists")
								.withDetail("Asset with assetId %s already exists".formatted(assetCreateRequest.getAssetId()))
								.build();
						}

						assetRepository.save(toEntity(assetCreateRequest, PRIVATE, municipalityId));
					} catch (final Exception e) {
						if (e instanceof final ThrowableProblem p) {
							errorDetail = ofNullable(p.getDetail());
						} else {
							errorDetail = ofNullable(e.getMessage());
						}
					}
				} else {
					errorDetail = ofNullable(constraintViolations.stream()
						.map(cv -> cv.getPropertyPath().toString() + " " + cv.getMessage())
						.collect(Collectors.joining(", ")));
				}

				// We have a failed row - copy it and the error details
				if (errorDetail.isPresent()) {
					copyRow(row, failedEntriesSheet, lastFailedRowIndex++, errorDetail);
				}
			}
		}

		return result
			.withFailed(lastFailedRowIndex - 1)
			.withFailedExcelData(out.toByteArray());
	}

	/**
	 * Copies the given source row to the target worksheet and sets the background to gray.
	 *
	 * @param sourceRow the source row.
	 * @param target    the target worksheet.
	 */
	private void copyHeaderRow(final Row sourceRow, final Worksheet target) {
		copyRow(sourceRow, target, 0, Optional.empty());

		for (var colIndex = 0; colIndex < sourceRow.getCellCount(); colIndex++) {
			target.style(0, colIndex).fillColor(Color.GRAY3).set();
		}
	}

	/**
	 * Copies the given source row to the given row index in the target worksheet, optionally adding
	 * "details" column at the end of the row.
	 *
	 * @param sourceRow      the source row.
	 * @param target         the target worksheet.
	 * @param targetRowIndex the target row index.
	 * @param optionalDetail an {@code Optional} that may hold additional "details" that is added to
	 *                       the end of the row if non-empty.
	 */
	private void copyRow(final Row sourceRow, final Worksheet target, final int targetRowIndex, final Optional<String> optionalDetail) {
		final var columnCount = sourceRow.getCellCount();

		for (var colIndex = 0; colIndex < columnCount; colIndex++) {
			// Handle date columns
			if (targetRowIndex > 0 && colIndex >= COL_ISSUED_DATE && colIndex <= COL_CARD_PRINTED) {
				final var currentColIndex = colIndex;

				sourceRow.getCellAsDate(colIndex)
					.map(LocalDateTime::toLocalDate)
					.ifPresent(date -> target.value(targetRowIndex, currentColIndex, date));
			} else {
				target.value(targetRowIndex, colIndex, sourceRow.getCellText(colIndex));
			}
		}

		optionalDetail.ifPresent(detail -> {
			target.value(targetRowIndex, columnCount + 1, detail);
			target.style(targetRowIndex, columnCount + 1).fontColor(Color.RED).set();
		});
	}

	/**
	 * Extracts the legal id from the given row (column 10, "PERSONNR") and adds a century digit if
	 * needed.
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the legal id, or is empty.
	 */
	Optional<String> extractLegalId(final Row row) {
		return extractCell(row, COL_LEGAL_ID)
			.map(this::cleanLegalId)
			.filter(not(String::isBlank))
			.map(legalId -> {
				// Strip off any century digits, if present
				if (legalId.matches("^\\d{12}$")) {
					return legalId.substring(2);
				}
				return legalId;
			});
	}

	/**
	 * Cleans and returns the provided legal id, removing everything but digits.
	 *
	 * @param  legalId the legal id to clean.
	 * @return         a legal id with digits only
	 */
	String cleanLegalId(final String legalId) {
		return legalId.replaceAll("\\D", "");
	}

	/**
	 * Extracts the asset id from the given row (column 7, "TILLSTNR").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the asset id, or is empty.
	 */
	Optional<String> extractAssetId(final Row row) {
		return extractCell(row, COL_ASSET_ID);
	}

	/**
	 * Extracts the issued date from the given row (column 16, "UTFARDAT").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the issued date, or is empty.
	 */
	Optional<LocalDate> extractIssuedDate(final Row row) {
		return row.getCellAsDate(COL_ISSUED_DATE).map(LocalDateTime::toLocalDate);
	}

	/**
	 * Extracts the valid-to date from the given row (column 18, "GILTIGTTOM").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the valid-to date, or is empty.
	 */
	Optional<LocalDate> extractValidToDate(final Row row) {
		return row.getCellAsDate(COL_VALID_TO_DATE).map(LocalDateTime::toLocalDate);
	}

	/**
	 * Extracts the status from the given row, by checking whether the valid-to date is after today
	 * or not.
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the status, or is empty.
	 */
	Optional<Status> extractStatus(final Row row) {
		return extractValidToDate(row)
			.map(validToDate -> validToDate.isAfter(LocalDate.now()) ? Status.ACTIVE : Status.EXPIRED);
	}

	/**
	 * Extracts the registration number from the given row (column 15, "DIARIENR").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the registration number, or is empty.
	 */
	Optional<String> extractRegistrationNumber(final Row row) {
		return extractCell(row, COL_REGISTRATION_NUMBER);
	}

	/**
	 * Extracts the date the card was printed from the given row (column 21, "UTSKRIVET").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the date the card was printed, or is empty.
	 */
	Optional<LocalDateTime> extractCardPrinted(final Row row) {
		return row.getCellAsDate(COL_CARD_PRINTED);
	}

	/**
	 * Extracts the "SmartCardSync" flag from the given row (column 27, "SmartParkSync").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains value of the "SmartParkSync" flag, or is empty.
	 */
	Optional<String> extractSmartParkSync(final Row row) {
		return extractCell(row, COL_SMART_PARK_SYNC)
			.flatMap(this::safeParseInt)
			.map(intValue -> switch (intValue)
			{
				case 0 -> "false";
				case 1 -> "true";
				default -> null;
			});
	}

	/**
	 * Extracts the (name of the) administration that issued the card from the given row (column 23, "EXTRA1").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the (name of the) administration that issued
	 *             the card, or is empty.
	 */
	Optional<String> extractIssuedByAdministration(final Row row) {
		return extractCell(row, COL_ISSUED_BY_ADMINISTRATION);
	}

	/**
	 * Extracts the (name of the) administrator that issued the card from the given row (column 24, "EXTRA2").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the (name of the) administrator that issued
	 *             the card, or is empty.
	 */
	Optional<String> extractIssuedByAdministrator(final Row row) {
		return extractCell(row, COL_ISSUED_BY_ADMINISTRATOR);
	}

	/**
	 * Extracts whether the card was applied for as a driver or passenger from the given row (column 5, "PASSAGE").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains whether the card was applied for as a driver
	 *             or passenger, or is empty.
	 */
	Optional<String> extractAppliedAs(final Row row) {
		return extractCell(row, COL_APPLIED_AS)
			.flatMap(this::safeParseInt)
			.map(intValue -> switch (intValue)
			{
				case 1 -> PASSENGER;
				case 2 -> DRIVER;
				default -> null;
			});
	}

	/**
	 * Extracts the sex as "K" for women and "M" for men, from the given row (column 4, "KON").
	 *
	 * @param  row the row.
	 * @return     an {@code Optional} that either contains the sex, or is empty.
	 */
	Optional<String> extractSex(final Row row) {
		return extractCell(row, COL_SEX)
			.flatMap(this::safeParseInt)
			.map(intValue -> switch (intValue)
			{
				case 0 -> "K";
				case 1 -> "M";
				default -> null;
			});
	}

	private Optional<Integer> safeParseInt(final String value) {
		try {
			return Optional.of(Integer.parseInt(value));
		} catch (final NumberFormatException e) {
			return Optional.empty();
		}
	}

	/**
	 * Convenience method to extract the contents of a cell as a string, regardless of what type
	 * Excel treats it as.
	 *
	 * @param  row       the row.
	 * @param  cellIndex the cell index.
	 * @return           an {@code Optional} that either contains the cell value as a string, or is empty
	 */
	Optional<String> extractCell(final Row row, final int cellIndex) {
		return Optional.of(row.getCellText(cellIndex)).filter(not(String::isBlank));
	}

	static class Result {

		private int total;

		private int failed;

		@JsonIgnore
		private byte[] failedExcelData;

		public int getTotal() {
			return total;
		}

		void setTotal(final int total) {
			this.total = total;
		}

		Result withTotal(final int total) {
			this.total = total;
			return this;
		}

		public int getFailed() {
			return failed;
		}

		void setFailed(final int failed) {
			this.failed = failed;
		}

		Result withFailed(final int failed) {
			this.failed = failed;
			return this;
		}

		public int getSuccessful() {
			return total - failed;
		}

		byte[] getFailedExcelData() {
			return failedExcelData;
		}

		void setFailedExcelData(final byte[] failedExcelData) {
			this.failedExcelData = failedExcelData;
		}

		Result withFailedExcelData(final byte[] failedExcelData) {
			this.failedExcelData = failedExcelData;
			return this;
		}

	}

}
