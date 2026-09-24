package se.sundsvall.partyassets.service.mapper;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.partyassets.api.model.AssetAttachment;
import se.sundsvall.partyassets.api.model.AssetAttachmentUpdateRequest;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentDataEntity;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

public final class AssetAttachmentMapper {

	private static final int MAX_MIME_TYPE_LENGTH = 255;

	private AssetAttachmentMapper() {}

	// The content stream is passed in rather than opened here: the blob only wraps it, so whoever opened it has to keep
	// it open until the insert is flushed and close it afterwards.
	public static AssetAttachmentEntity toAssetAttachmentEntity(final AssetEntity asset, final MultipartFile file, final InputStream content, final String category, final String description) {
		return AssetAttachmentEntity.create()
			.withAsset(asset)
			.withAttachmentData(AssetAttachmentDataEntity.create()
				.withFile(Hibernate.getLobHelper().createBlob(content, file.getSize())))
			.withMunicipalityId(asset.getMunicipalityId())
			.withFileName(StringUtils.getFilename(file.getOriginalFilename()))
			.withMimeType(toMimeType(file.getContentType()))
			.withFileSize(Math.toIntExact(file.getSize()))
			.withCategory(category)
			.withDescription(description);
	}

	private static String toMimeType(final String contentType) {
		try {
			return MediaType.parseMediaType(contentType).isConcrete() && contentType.length() <= MAX_MIME_TYPE_LENGTH ? contentType : APPLICATION_OCTET_STREAM_VALUE;
		} catch (final InvalidMediaTypeException e) {
			return APPLICATION_OCTET_STREAM_VALUE;
		}
	}

	// The MariaDB driver materializes a blob in memory, so copying one costs its full size in heap for the duration of
	// the copy - a renewal of an asset carrying several drawings is the expensive case.
	public static AssetAttachmentDataEntity copyAssetAttachmentData(final AssetAttachmentDataEntity data) {
		if (data == null || data.getFile() == null) {
			return null;
		}

		try {
			return AssetAttachmentDataEntity.create()
				.withFile(Hibernate.getLobHelper().createBlob(data.getFile().getBinaryStream(), data.getFile().length()));
		} catch (final SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Could not copy attachment data: %s".formatted(e.getMessage()));
		}
	}

	public static AssetAttachment toAssetAttachment(final AssetAttachmentEntity entity) {
		if (entity == null) {
			return null;
		}

		return AssetAttachment.create()
			.withId(entity.getId())
			.withFileName(entity.getFileName())
			.withMimeType(entity.getMimeType())
			.withFileSize(entity.getFileSize())
			.withCategory(entity.getCategory())
			.withDescription(entity.getDescription())
			.withCreated(entity.getCreated())
			.withUpdated(entity.getUpdated());
	}

	public static List<AssetAttachment> toAssetAttachments(final List<AssetAttachmentEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(AssetAttachmentMapper::toAssetAttachment)
			.filter(Objects::nonNull)
			.toList();
	}

	public static AssetAttachmentEntity updateEntity(final AssetAttachmentEntity entity, final AssetAttachmentUpdateRequest request) {
		if (request == null) {
			return entity;
		}

		ofNullable(request.getFileName()).ifPresent(entity::setFileName);
		ofNullable(request.getCategory()).ifPresent(entity::setCategory);
		ofNullable(request.getDescription()).ifPresent(entity::setDescription);

		return entity;
	}
}
