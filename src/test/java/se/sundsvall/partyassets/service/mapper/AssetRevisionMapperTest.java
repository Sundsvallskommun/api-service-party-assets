package se.sundsvall.partyassets.service.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;
import se.sundsvall.partyassets.integration.db.model.PartyType;

import static java.util.UUID.randomUUID;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toAssetRevision;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toRevision;

class AssetRevisionMapperTest {

	@AfterEach
	void clearIdentifier() {
		Identifier.remove();
	}

	@Test
	void toRevisionCopiesEveryScalarField() {
		final var id = randomUUID().toString();
		final var entity = getAssetEntity(id, randomUUID().toString()).withPartyType(PartyType.PRIVATE).withRevision(2);

		final var revision = toRevision(entity);

		assertThat(revision.getAssetId()).isEqualTo(id);
		assertThat(revision.getRevision()).isEqualTo(2);
		assertThat(revision.getActor()).isEqualTo("previous.actor");
		assertThat(revision.getMunicipalityId()).isEqualTo(entity.getMunicipalityId());
		assertThat(revision.getExternalAssetId()).isEqualTo(entity.getAssetId());
		assertThat(revision.getPartyId()).isEqualTo(entity.getPartyId());
		assertThat(revision.getPartyType()).isEqualTo("PRIVATE");
		assertThat(revision.getType()).isEqualTo(entity.getType());
		assertThat(revision.getIssued()).isEqualTo(entity.getIssued());
		assertThat(revision.getValidTo()).isEqualTo(entity.getValidTo());
		assertThat(revision.getStatus()).isEqualTo("ACTIVE");
		assertThat(revision.getStatusReason()).isEqualTo(entity.getStatusReason());
		assertThat(revision.getDescription()).isEqualTo(entity.getDescription());
	}

	@Test
	void toRevisionSerializesTheChildren() {
		final var entity = getAssetEntity(randomUUID().toString(), randomUUID().toString());

		final var revision = toRevision(entity);

		assertThatJson(revision.getAdditionalParameters()).isEqualTo("{\"key\":\"value\"}");
		assertThatJson(revision.getCaseReferenceIds()).isEqualTo("[\"caseReferenceId\"]");
		assertThatJson(revision.getJsonParameters()).isEqualTo("[{\"key\":\"key1\",\"schemaId\":\"2281_person_schema_1.0.0\",\"value\":{}}]");
		assertThatJson(revision.getAttachments()).isEqualTo("[]");
	}

	// The canary for Jackson's date handling. An attachment timestamp has to survive the round trip through the column,
	// or a revision cannot report the metadata it had.
	@Test
	void toRevisionRoundTripsAnAttachmentTimestamp() {
		final var created = OffsetDateTime.of(2023, 1, 2, 12, 0, 0, 0, ZoneOffset.ofHours(2));
		final var entity = getAssetEntity(randomUUID().toString(), randomUUID().toString())
			.withAttachments(List.of(AssetAttachmentEntity.create()
				.withId("attachment-1")
				.withFileName("lokalritning.pdf")
				.withMimeType("application/pdf")
				.withFileSize(1024)
				.withCategory("LOKALRITNING")
				.withCreated(created)));

		final var readBack = toAssetRevision(toRevision(entity)).getAttachments();

		assertThat(readBack).singleElement().satisfies(attachment -> {
			assertThat(attachment.getId()).isEqualTo("attachment-1");
			assertThat(attachment.getFileName()).isEqualTo("lokalritning.pdf");
			assertThat(attachment.getFileSize()).isEqualTo(1024);
			// The offset has to survive too, not just the instant. AssertJ compares OffsetDateTime by instant, so
			// asserting the offset separately is what catches a rewrite of 12:00+02:00 into 10:00Z.
			assertThat(attachment.getCreated()).isEqualTo(created);
			assertThat(attachment.getCreated().getOffset()).isEqualTo(created.getOffset());
		});
	}

	@Test
	void toRevisionSkipsSoftDeletedAttachments() {
		final var entity = getAssetEntity(randomUUID().toString(), randomUUID().toString())
			.withAttachments(List.of(
				AssetAttachmentEntity.create().withId("kept").withFileName("kept.pdf"),
				AssetAttachmentEntity.create().withId("gone").withFileName("gone.pdf").withDeleted(true)));

		assertThatJson(toRevision(entity).getAttachments())
			.isEqualTo("[{\"id\":\"kept\",\"fileName\":\"kept.pdf\",\"mimeType\":null,\"fileSize\":null,\"category\":null,\"description\":null,\"created\":null,\"updated\":null}]");
	}

	@Test
	void toRevisionWithNullCollections() {
		final var entity = AssetEntity.create();

		final var revision = toRevision(entity);

		assertThat(revision.getAdditionalParameters()).isEqualTo("{}");
		assertThat(revision.getCaseReferenceIds()).isEqualTo("[]");
		assertThat(revision.getJsonParameters()).isEqualTo("[]");
		assertThat(revision.getAttachments()).isEqualTo("[]");
		assertThat(revision.getStatus()).isNull();
		assertThat(revision.getPartyType()).isNull();
	}

	// The point of the whole feature: an asset snapshotted and read back has to describe the same thing the live asset
	// does. If this drifts, every revision in the database is quietly wrong.
	@Test
	void toAssetRevisionRoundTripsThroughToRevision() {
		final var id = randomUUID().toString();
		final var entity = getAssetEntity(id, randomUUID().toString())
			.withPartyType(PartyType.PRIVATE)
			.withRevision(2)
			.withAttachments(List.of(AssetAttachmentEntity.create()
				.withId("attachment-1")
				.withFileName("lokalritning.pdf")
				.withCreated(OffsetDateTime.of(2023, 1, 2, 12, 0, 0, 0, ZoneOffset.ofHours(2)))));

		final var fromSnapshot = toAssetRevision(toRevision(entity));
		final var fromAsset = toAssetRevision(entity);

		assertThat(fromSnapshot)
			.usingRecursiveComparison()
			// recordedAt is when the snapshot was taken, which has no counterpart on the live asset.
			.ignoringFields("recordedAt")
			.isEqualTo(fromAsset);
	}

	@Test
	void toAssetRevisionWithNullJsonColumns() {
		final var revision = toAssetRevision(AssetRevisionEntity.create().withAssetId("asset-1").withRevision(0));

		assertThat(revision.getAdditionalParameters()).isNull();
		assertThat(revision.getJsonParameters()).isNull();
		assertThat(revision.getAttachments()).isNull();
		assertThat(revision.getStatus()).isNull();
	}

	// The column is a varchar precisely so a value dropped from the enum cannot make old snapshots unreadable.
	@Test
	void toAssetRevisionWithAnUnknownStatusYieldsNull() {
		final var revision = toAssetRevision(AssetRevisionEntity.create()
			.withAssetId("asset-1")
			.withRevision(0)
			.withStatus("A_STATUS_THAT_NO_LONGER_EXISTS"));

		assertThat(revision.getStatus()).isNull();
	}

	@Test
	void toAssetRevisionFromAnAssetUsesCreatedWhenUpdatedIsNull() {
		final var created = OffsetDateTime.now().minusDays(5);
		final var asset = AssetEntity.create().withId("asset-1").withRevision(0).withCreated(created).withUpdated(null);

		assertThat(toAssetRevision(asset).getRecordedAt()).isEqualTo(created);
	}

	@Test
	void currentActorReadsTheIdentifier() {
		Identifier.set(Identifier.parse("joe01doe; type=adAccount"));

		assertThat(currentActor()).isEqualTo("joe01doe");
	}

	@Test
	void currentActorIsNullWithoutAnIdentifier() {
		assertThat(currentActor()).isNull();
	}

	// The header needs both a value and a type to parse at all. A value on its own leaves no actor, silently.
	@Test
	void currentActorIsNullForAHeaderWithoutAType() {
		Identifier.set(Identifier.parse("joe01doe"));

		assertThat(currentActor()).isNull();
	}
}
