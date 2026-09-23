package se.sundsvall.partyassets.service.mapper;

import java.util.List;
import java.util.Map;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.partyassets.api.model.AssetAttachment;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.api.model.AssetRevision;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachments;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toAssetJsonParameterList;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.cfg.DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;

public final class AssetRevisionMapper {

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
		.disable(FAIL_ON_UNKNOWN_PROPERTIES)
		.disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
		.build();

	private static final int MAX_ACTOR_LENGTH = 255;

	private AssetRevisionMapper() {}

	public static AssetRevisionEntity toRevision(final AssetEntity asset) {
		return AssetRevisionEntity.create()
			.withAssetId(asset.getId())
			.withRevision(asset.getRevision())
			.withActor(asset.getActor())
			.withMunicipalityId(asset.getMunicipalityId())
			.withOrigin(asset.getOrigin())
			.withExternalAssetId(asset.getAssetId())
			.withPartyId(asset.getPartyId())
			.withPartyType(name(asset.getPartyType()))
			.withType(asset.getType())
			.withIssued(asset.getIssued())
			.withValidTo(asset.getValidTo())
			.withReplacesId(asset.getReplacesId())
			.withStatus(name(asset.getStatus()))
			.withStatusReason(asset.getStatusReason())
			.withDescription(asset.getDescription())
			.withTitle(asset.getTitle())
			.withAdditionalParameters(toJson(ofNullable(asset.getAdditionalParameters()).orElse(emptyMap())))
			.withCaseReferenceIds(toJson(ofNullable(asset.getCaseReferenceIds()).orElse(emptyList())))
			.withJsonParameters(toJson(toAssetJsonParameterList(asset.getJsonParameters())))
			.withAttachments(toJson(toAssetAttachments(presentAttachments(asset.getAttachments()))));
	}

	public static String currentActor() {
		return ofNullable(Identifier.get())
			.map(Identifier::getValue)
			.map(AssetRevisionMapper::validateActorLength)
			.orElse(null);
	}

	private static String validateActorLength(final String actor) {
		if (actor.length() > MAX_ACTOR_LENGTH) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Invalid actor")
				.withDetail("The value of the %s header must be at most %d characters".formatted(Identifier.HEADER_NAME, MAX_ACTOR_LENGTH))
				.build();
		}
		return actor;
	}

	private static List<AssetAttachmentEntity> presentAttachments(final List<AssetAttachmentEntity> attachments) {
		return ofNullable(attachments).orElse(emptyList()).stream()
			.filter(attachment -> !attachment.isDeleted())
			.toList();
	}

	public static AssetRevision toAssetRevision(final AssetRevisionEntity entity) {
		return AssetRevision.create()
			.withId(entity.getAssetId())
			.withRevision(entity.getRevision())
			.withActor(entity.getActor())
			.withRecordedAt(entity.getRecordedAt())
			.withAssetId(entity.getExternalAssetId())
			.withOrigin(entity.getOrigin())
			.withPartyId(entity.getPartyId())
			.withType(entity.getType())
			.withIssued(entity.getIssued())
			.withValidTo(entity.getValidTo())
			.withStatus(toStatus(entity.getStatus()))
			.withStatusReason(entity.getStatusReason())
			.withDescription(entity.getDescription())
			.withTitle(entity.getTitle())
			.withReplacesId(entity.getReplacesId())
			.withAdditionalParameters(fromJson(entity.getAdditionalParameters(), new TypeReference<Map<String, String>>() {}))
			.withJsonParameters(fromJson(entity.getJsonParameters(), new TypeReference<List<AssetJsonParameter>>() {}))
			.withAttachments(fromJson(entity.getAttachments(), new TypeReference<List<AssetAttachment>>() {}));
	}

	public static AssetRevision toAssetRevision(final AssetEntity asset) {
		return AssetRevision.create()
			.withId(asset.getId())
			.withRevision(asset.getRevision())
			.withActor(asset.getActor())
			.withRecordedAt(ofNullable(asset.getUpdated()).orElse(asset.getCreated()))
			.withAssetId(asset.getAssetId())
			.withOrigin(asset.getOrigin())
			.withPartyId(asset.getPartyId())
			.withType(asset.getType())
			.withIssued(asset.getIssued())
			.withValidTo(asset.getValidTo())
			.withStatus(asset.getStatus())
			.withStatusReason(asset.getStatusReason())
			.withDescription(asset.getDescription())
			.withTitle(asset.getTitle())
			.withReplacesId(asset.getReplacesId())
			.withAdditionalParameters(asset.getAdditionalParameters())
			.withJsonParameters(toAssetJsonParameterList(asset.getJsonParameters()))
			.withAttachments(toAssetAttachments(presentAttachments(asset.getAttachments())));
	}

	private static String toJson(final Object value) {
		return OBJECT_MAPPER.writeValueAsString(value);
	}

	private static <T> T fromJson(final String json, final TypeReference<T> type) {
		return ofNullable(json).map(value -> OBJECT_MAPPER.<T>readValue(value, type)).orElse(null);
	}

	private static String name(final Enum<?> value) {
		return ofNullable(value).map(Enum::name).orElse(null);
	}

	private static Status toStatus(final String value) {
		try {
			return ofNullable(value).map(Status::valueOf).orElse(null);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
