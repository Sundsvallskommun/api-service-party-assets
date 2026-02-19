package se.sundsvall.partyassets.pr3import;

import java.time.LocalDateTime;
import java.util.Optional;
import org.dhatim.fastexcel.reader.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.EXPIRED;
import static se.sundsvall.partyassets.pr3import.PR3Importer.DRIVER;
import static se.sundsvall.partyassets.pr3import.PR3Importer.PASSENGER;

@ExtendWith(MockitoExtension.class)
class PR3ImporterExtractionTest {

	@Mock
	private Row mockRow;

	private final PR3Importer importer = new PR3Importer(null, null, null, null);

	@Test
	void extractAssetId() {
		when(mockRow.getCellText(7)).thenReturn("someAssetId");

		var result = importer.extractAssetId(mockRow);

		assertThat(result).isNotEmpty().hasValue("someAssetId");

		verify(mockRow).getCellText(7);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractIssuedDate() {
		var now = LocalDateTime.now();

		when(mockRow.getCellAsDate(16)).thenReturn(Optional.of(now));

		var result = importer.extractIssuedDate(mockRow);

		assertThat(result).isNotEmpty().hasValue(now.toLocalDate());

		verify(mockRow).getCellAsDate(16);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractValidToDate() {
		var now = LocalDateTime.now();

		when(mockRow.getCellAsDate(18)).thenReturn(Optional.of(now));

		var result = importer.extractValidToDate(mockRow);

		assertThat(result).isNotEmpty().hasValue(now.toLocalDate());

		verify(mockRow).getCellAsDate(18);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractStatusForPastDate() {
		var validTo = LocalDateTime.now().minusDays(5);

		when(mockRow.getCellAsDate(18)).thenReturn(Optional.of(validTo));

		var result = importer.extractStatus(mockRow);

		assertThat(result).isNotEmpty().hasValue(EXPIRED);

		verify(mockRow).getCellAsDate(18);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractStatusForLaterDate() {
		var validTo = LocalDateTime.now().plusDays(5);

		when(mockRow.getCellAsDate(18)).thenReturn(Optional.of(validTo));

		var result = importer.extractStatus(mockRow);

		assertThat(result).isNotEmpty().hasValue(ACTIVE);

		verify(mockRow).getCellAsDate(18);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractRegistrationNumber() {
		when(mockRow.getCellText(15)).thenReturn("someRegistrationNumber");

		var result = importer.extractRegistrationNumber(mockRow);

		assertThat(result).isNotEmpty().hasValue("someRegistrationNumber");

		verify(mockRow).getCellText(15);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractCardPrinted() {
		var now = LocalDateTime.now();

		when(mockRow.getCellAsDate(21)).thenReturn(Optional.of(now));

		var result = importer.extractCardPrinted(mockRow);

		assertThat(result).isNotEmpty().hasValue(now);

		verify(mockRow).getCellAsDate(21);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractSmartParkSyncForZero() {
		when(mockRow.getCellText(27)).thenReturn("0");

		var result = importer.extractSmartParkSync(mockRow);

		assertThat(result).isNotEmpty().hasValue("false");

		verify(mockRow).getCellText(27);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractSmartParkSyncForOne() {
		when(mockRow.getCellText(27)).thenReturn("1");

		var result = importer.extractSmartParkSync(mockRow);

		assertThat(result).isNotEmpty().hasValue("true");

		verify(mockRow).getCellText(27);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractIssuedByAdministration() {
		when(mockRow.getCellText(23)).thenReturn("someAdministration");

		var result = importer.extractIssuedByAdministration(mockRow);

		assertThat(result).isNotEmpty().hasValue("someAdministration");

		verify(mockRow).getCellText(23);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractIssuedByAdministrator() {
		when(mockRow.getCellText(24)).thenReturn("someAdministrator");

		var result = importer.extractIssuedByAdministrator(mockRow);

		assertThat(result).isNotEmpty().hasValue("someAdministrator");

		verify(mockRow).getCellText(24);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractAppliedAsForOne() {
		when(mockRow.getCellText(5)).thenReturn("1");

		var result = importer.extractAppliedAs(mockRow);

		assertThat(result).isNotEmpty().hasValue(PASSENGER);

		verify(mockRow).getCellText(5);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractAppliedAsForTwo() {
		when(mockRow.getCellText(5)).thenReturn("2");

		var result = importer.extractAppliedAs(mockRow);

		assertThat(result).isNotEmpty().hasValue(DRIVER);

		verify(mockRow).getCellText(5);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractSexForZero() {
		when(mockRow.getCellText(4)).thenReturn("0");

		var result = importer.extractSex(mockRow);

		assertThat(result).isNotEmpty().hasValue("K");

		verify(mockRow).getCellText(4);
		verifyNoMoreInteractions(mockRow);
	}

	@Test
	void extractSexForOne() {
		when(mockRow.getCellText(4)).thenReturn("1");

		var result = importer.extractSex(mockRow);

		assertThat(result).isNotEmpty().hasValue("M");

		verify(mockRow).getCellText(4);
		verifyNoMoreInteractions(mockRow);
	}
}
