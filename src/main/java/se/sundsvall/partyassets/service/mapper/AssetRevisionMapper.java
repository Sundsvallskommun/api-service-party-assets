package se.sundsvall.partyassets.service.mapper;

import java.util.List;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachments;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toAssetJsonParameterList;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public final class AssetRevisionMapper {

	// Unknown properties are ignored on the way in: a field removed from the API models later must not make every older
	// snapshot unreadable.
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
		.disable(FAIL_ON_UNKNOWN_PROPERTIES)
		.build();

	private AssetRevisionMapper() {}

	/**
	 * Snapshots an asset as it is right now. Call this before applying a change, never after.
	 */
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
			.withAdditionalParameters(toJson(ofNullable(asset.getAdditionalParameters()).orElse(emptyMap())))
			.withCaseReferenceIds(toJson(ofNullable(asset.getCaseReferenceIds()).orElse(emptyList())))
			.withJsonParameters(toJson(toAssetJsonParameterList(asset.getJsonParameters())))
			.withAttachments(toJson(toAssetAttachments(presentAttachments(asset.getAttachments()))));
	}

	// dept44 fills Identifier from the X-Sent-By header. Note that the header needs both a value and a type part to
	// parse at all, so a malformed one yields no actor rather than an error - as does no header, and the nightly job.
	public static String currentActor() {
		return ofNullable(Identifier.get()).map(Identifier::getValue).orElse(null);
	}

	// Reading the attachments initializes a lazy collection, which costs one extra select per write path. That is the
	// price of being able to show the attachment metadata a revision had.
	private static List<AssetAttachmentEntity> presentAttachments(final List<AssetAttachmentEntity> attachments) {
		return ofNullable(attachments).orElse(emptyList()).stream()
			.filter(attachment -> !attachment.isDeleted())
			.toList();
	}

	private static String toJson(final Object value) {
		return OBJECT_MAPPER.writeValueAsString(value);
	}

	private static String name(final Enum<?> value) {
		return ofNullable(value).map(Enum::name).orElse(null);
	}
}
