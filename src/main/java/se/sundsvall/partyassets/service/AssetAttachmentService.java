package se.sundsvall.partyassets.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetAttachment;
import se.sundsvall.partyassets.api.model.AssetAttachmentUpdateRequest;
import se.sundsvall.partyassets.integration.db.AssetAttachmentRepository;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachment;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachmentEntity;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachments;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.updateEntity;

@Service
@Transactional
public class AssetAttachmentService {

	private static final String ASSET_NOT_FOUND_TITLE = "Asset not found";
	private static final String ASSET_NOT_FOUND_DETAIL = "Asset with id %s not found for municipalityId %s";
	private static final String ATTACHMENT_NOT_FOUND_TITLE = "Attachment not found";
	private static final String ATTACHMENT_NOT_FOUND_DETAIL = "Attachment with id %s not found on asset %s for municipalityId %s";
	private static final String READ_FAILED_DETAIL = "Could not read data for attachment %s: %s";
	private static final int MAX_FILE_NAME_LENGTH = 255;

	private final AssetRepository assetRepository;
	private final AssetAttachmentRepository attachmentRepository;

	public AssetAttachmentService(final AssetRepository assetRepository, final AssetAttachmentRepository attachmentRepository) {
		this.assetRepository = assetRepository;
		this.attachmentRepository = attachmentRepository;
	}

	// The blob only wraps the upload stream and the driver reads it when the row is inserted, so the insert is flushed
	// while the stream is still open. A plain save would leave the read to happen after try-with-resources closed it.
	public String createAttachment(final String municipalityId, final String assetId, final MultipartFile file, final String category, final String description) {
		final var asset = getAssetEntity(municipalityId, assetId);
		validateAssetIsModifiable(asset);
		validateFile(file);

		try (final var content = file.getInputStream()) {
			return attachmentRepository.saveAndFlush(toAssetAttachmentEntity(asset, file, content, category, description)).getId();
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Could not read uploaded file %s: %s".formatted(file.getOriginalFilename(), e.getMessage()));
		}
	}

	@Transactional(readOnly = true)
	public List<AssetAttachment> readAttachments(final String municipalityId, final String assetId) {
		verifyAssetExists(municipalityId, assetId);

		return toAssetAttachments(attachmentRepository.findAllForAsset(assetId, municipalityId));
	}

	// The content is read here rather than streamed to the response: the driver already holds every byte in memory, so
	// writing inside the transaction would keep a database connection checked out for the whole network transfer.
	@Transactional(readOnly = true)
	public AssetAttachmentContent readAttachment(final String municipalityId, final String assetId, final String attachmentId) {
		final var attachment = getAttachmentEntity(municipalityId, assetId, attachmentId);

		try (final var content = attachment.getAttachmentData().getFile().getBinaryStream()) {
			return new AssetAttachmentContent(attachment.getFileName(), attachment.getMimeType(), content.readAllBytes());
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, READ_FAILED_DETAIL.formatted(attachmentId, e.getMessage()));
		}
	}

	// saveAndFlush, not save: the entity is already managed, so a plain save would let @PreUpdate fire at commit - after
	// the response has been mapped - and the caller would get back the timestamp from before the update.
	public AssetAttachment updateAttachment(final String municipalityId, final String assetId, final String attachmentId, final AssetAttachmentUpdateRequest request) {
		final var attachment = getAttachmentEntity(municipalityId, assetId, attachmentId);
		validateAssetIsModifiable(attachment.getAsset());

		return toAssetAttachment(attachmentRepository.saveAndFlush(updateEntity(attachment, request)));
	}

	public void deleteAttachment(final String municipalityId, final String assetId, final String attachmentId) {
		final var attachment = getAttachmentEntity(municipalityId, assetId, attachmentId);
		validateAssetIsModifiable(attachment.getAsset());

		attachmentRepository.delete(attachment);
	}

	private void validateAssetIsModifiable(final AssetEntity asset) {
		if (asset.getStatus() != DRAFT && asset.getStatus() != ACTIVE) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Attachments cannot be modified")
				.withDetail("Attachments can only be modified on assets with status %s or %s, but asset %s has status %s".formatted(DRAFT, ACTIVE, asset.getId(), asset.getStatus()))
				.build();
		}
	}

	private void validateFile(final MultipartFile file) {
		if (file.isEmpty()) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Invalid file")
				.withDetail("The uploaded file is empty")
				.build();
		}

		final var fileName = file.getOriginalFilename();
		if (fileName != null && fileName.length() > MAX_FILE_NAME_LENGTH) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Invalid file name")
				.withDetail("File name must not exceed %s characters".formatted(MAX_FILE_NAME_LENGTH))
				.build();
		}
	}

	private void verifyAssetExists(final String municipalityId, final String assetId) {
		if (!assetRepository.existsByIdAndMunicipalityId(assetId, municipalityId)) {
			throw assetNotFound(municipalityId, assetId);
		}
	}

	private AssetEntity getAssetEntity(final String municipalityId, final String assetId) {
		return assetRepository.findByIdAndMunicipalityId(assetId, municipalityId)
			.orElseThrow(() -> assetNotFound(municipalityId, assetId));
	}

	private AssetAttachmentEntity getAttachmentEntity(final String municipalityId, final String assetId, final String attachmentId) {
		verifyAssetExists(municipalityId, assetId);

		return attachmentRepository.findByIdForAsset(attachmentId, assetId, municipalityId)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ATTACHMENT_NOT_FOUND_TITLE)
				.withDetail(ATTACHMENT_NOT_FOUND_DETAIL.formatted(attachmentId, assetId, municipalityId))
				.build());
	}

	private ThrowableProblem assetNotFound(final String municipalityId, final String assetId) {
		return Problem.builder()
			.withStatus(NOT_FOUND)
			.withTitle(ASSET_NOT_FOUND_TITLE)
			.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(assetId, municipalityId))
			.build();
	}
}
