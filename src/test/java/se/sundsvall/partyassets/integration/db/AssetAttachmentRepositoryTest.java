package se.sundsvall.partyassets.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Asset attachment repository tests.
 *
 * @see src/test/resources/db/scripts/assetAttachmentRepositoryTest.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetAttachmentRepositoryTest.sql"
})
class AssetAttachmentRepositoryTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String OTHER_MUNICIPALITY_ID = "2260";
	private static final String ASSET_ID = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";
	private static final String OTHER_ASSET_ID = "945576d3-6e92-4118-ba33-53582d338ad3";
	private static final String ATTACHMENT_ID = "7c145278-da81-49b0-a011-0f8f6821e3a0";
	private static final String SECOND_ATTACHMENT_ID = "647e3062-62dc-499f-9faa-e54cb97aa214";
	private static final String OTHER_ATTACHMENT_ID = "cba6f0e5-e826-4690-8776-37c69d981a2a";
	private static final String DELETED_ATTACHMENT_ID = "d1e2f3a4-5b6c-7d8e-9f01-2a3b4c5d6e7f";

	@Autowired
	private AssetAttachmentRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void findAllForAsset() {
		final var result = repository.findAllForAsset(ASSET_ID, MUNICIPALITY_ID);

		assertThat(result)
			.extracting(attachment -> attachment.getId(), attachment -> attachment.getFileName())
			.containsExactly(
				tuple(ATTACHMENT_ID, "lokalritning.pdf"),
				tuple(SECOND_ATTACHMENT_ID, "planritning.pdf"));
	}

	// The query filters on the asset primary key, not on AssetEntity.assetId - passing the external business id must
	// therefore find nothing.
	@Test
	void findAllForAssetDoesNotMatchTheExternalAssetId() {
		assertThat(repository.findAllForAsset("PRH-0000000001", MUNICIPALITY_ID)).isEmpty();
	}

	@Test
	void findAllForAssetWithForeignMunicipalityId() {
		assertThat(repository.findAllForAsset(ASSET_ID, OTHER_MUNICIPALITY_ID)).isEmpty();
	}

	@Test
	void findByIdForAsset() {
		final var result = repository.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);

		assertThat(result).hasValueSatisfying(attachment -> {
			assertThat(attachment.getFileName()).isEqualTo("lokalritning.pdf");
			assertThat(attachment.getMimeType()).isEqualTo("application/pdf");
			assertThat(attachment.getAsset().getId()).isEqualTo(ASSET_ID);
		});
	}

	@Test
	void findByIdForAssetWhenTheAttachmentBelongsToAnotherAsset() {
		assertThat(repository.findByIdForAsset(OTHER_ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).isEmpty();
		assertThat(repository.findByIdForAsset(OTHER_ATTACHMENT_ID, OTHER_ASSET_ID, MUNICIPALITY_ID)).isPresent();
	}

	@Test
	void findByIdForAssetWithForeignMunicipalityId() {
		assertThat(repository.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, OTHER_MUNICIPALITY_ID)).isEmpty();
	}

	@Test
	void findAllForAssetSkipsSoftDeleted() {
		assertThat(repository.findAllForAsset(ASSET_ID, MUNICIPALITY_ID))
			.extracting(AssetAttachmentEntity::getFileName)
			.doesNotContain("raderad.pdf");
	}

	@Test
	void findByIdForAssetSkipsSoftDeleted() {
		assertThat(repository.findByIdForAsset(DELETED_ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).isEmpty();
	}

	// The bytes of a deleted attachment have to stay reachable, or an older revision points at a file that is gone.
	@Test
	void findByIdForAssetIncludingDeletedFindsASoftDeletedAttachment() {
		assertThat(repository.findByIdForAssetIncludingDeleted(DELETED_ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID))
			.hasValueSatisfying(attachment -> {
				assertThat(attachment.isDeleted()).isTrue();
				assertThat(attachment.getFileName()).isEqualTo("raderad.pdf");
				assertThat(attachment.getAttachmentData()).isNotNull();
			});
	}

	// Still the right behaviour for the cascade from deleting a whole asset; the soft delete lives in the service.
	@Test
	void deleteRemovesTheAttachmentDataRow() {
		final var attachment = repository.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID).orElseThrow();
		final var dataId = attachment.getAttachmentData().getId();

		repository.delete(attachment);
		repository.flush();

		assertThat(repository.findById(ATTACHMENT_ID)).isEmpty();
		assertThat(jdbcTemplate.queryForObject("select count(*) from asset_attachment_data where id = ?", Long.class, dataId)).isZero();
	}
}
