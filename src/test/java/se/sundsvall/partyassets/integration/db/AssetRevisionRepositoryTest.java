package se.sundsvall.partyassets.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Asset revision repository tests.
 *
 * @see src/test/resources/db/scripts/assetRevisionRepositoryTest.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetRevisionRepositoryTest.sql"
})
class AssetRevisionRepositoryTest {

	private static final String ASSET_ID = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";
	private static final String ASSET_ID_WITH_GAP = "cba6f0e5-e826-4690-8776-37c69d981a2a";

	@Autowired
	private AssetRevisionRepository repository;

	@Test
	void findByAssetIdOrderByRevisionDesc() {
		final var result = repository.findByAssetIdOrderByRevisionDesc(ASSET_ID);

		assertThat(result)
			.extracting(AssetRevisionEntity::getRevision, AssetRevisionEntity::getActor, AssetRevisionEntity::getStatus)
			.containsExactly(
				tuple(2, "third.actor", "BLOCKED"),
				tuple(1, "second.actor", "ACTIVE"),
				tuple(0, null, "ACTIVE"));
	}

	@Test
	void findByAssetIdOrderByRevisionDescForUnknownAsset() {
		assertThat(repository.findByAssetIdOrderByRevisionDesc("f0000000-0000-0000-0000-000000000000")).isEmpty();
	}

	@Test
	void findByAssetIdAndRevision() {
		final var result = repository.findByAssetIdAndRevision(ASSET_ID, 2);

		assertThat(result).hasValueSatisfying(revision -> {
			assertThat(revision.getExternalAssetId()).isEqualTo("PRH-0000000001");
			assertThat(revision.getStatusReason()).isEqualTo("Stöldanmäld");
			assertThat(revision.getAdditionalParameters()).isEqualTo("{\"foo\":\"bar\"}");
			assertThat(revision.getCaseReferenceIds()).isEqualTo("[\"case-1\"]");
		});
	}

	// A gap in the numbering is what a write path that mutated without snapshotting leaves behind. It has to read as a
	// miss rather than silently resolve to a neighbouring revision.
	@Test
	void findByAssetIdAndRevisionForAGapInTheNumbering() {
		assertThat(repository.findByAssetIdAndRevision(ASSET_ID_WITH_GAP, 1)).isEmpty();
	}

	@Test
	void saveAssignsAnIdAndRecordedAt() {
		final var saved = repository.saveAndFlush(AssetRevisionEntity.create()
			.withAssetId(ASSET_ID)
			.withRevision(7)
			.withStatus("ACTIVE"));

		assertThat(saved.getId()).isNotBlank();
		assertThat(saved.getRecordedAt()).isNotNull();
	}

	@Test
	void duplicateRevisionForTheSameAssetIsRejected() {
		final var duplicate = AssetRevisionEntity.create()
			.withAssetId(ASSET_ID)
			.withRevision(2)
			.withStatus("ACTIVE");

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> repository.saveAndFlush(duplicate));
	}
}
