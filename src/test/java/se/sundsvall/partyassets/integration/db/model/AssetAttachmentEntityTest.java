package se.sundsvall.partyassets.integration.db.model;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbBlob;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;

class AssetAttachmentEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AssetAttachmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToStringExcluding("asset", "attachmentData"),
			hasValidBeanEqualsExcluding("asset", "attachmentData"),
			hasValidBeanHashCodeExcluding("asset", "attachmentData")));
	}

	@Test
	void testBuilderMethods() {

		final var asset = AssetEntity.create();
		final var attachmentData = AssetAttachmentDataEntity.create().withFile(new MariaDbBlob("file".getBytes()));
		final var id = "id";
		final var municipalityId = "2281";
		final var fileName = "drawing.pdf";
		final var mimeType = "application/pdf";
		final var fileSize = 1024;
		final var category = "LOKALRITNING";
		final var description = "Ritning över serveringslokal";
		final var created = now();
		final var updated = now().plusDays(1);
		final var deleted = true;

		final var bean = AssetAttachmentEntity.create()
			.withDeleted(deleted)
			.withAsset(asset)
			.withAttachmentData(attachmentData)
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withFileName(fileName)
			.withMimeType(mimeType)
			.withFileSize(fileSize)
			.withCategory(category)
			.withDescription(description)
			.withCreated(created)
			.withUpdated(updated);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAsset()).isEqualTo(asset);
		assertThat(bean.getAttachmentData()).isEqualTo(attachmentData);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getFileName()).isEqualTo(fileName);
		assertThat(bean.getMimeType()).isEqualTo(mimeType);
		assertThat(bean.getFileSize()).isEqualTo(fileSize);
		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.isDeleted()).isEqualTo(deleted);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	// deleted is a primitive, so it is false rather than null on a fresh bean - a wrapper would have made soft deletion
	// three-valued and forced every query predicate to handle null.
	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetAttachmentEntity.create()).hasAllNullFieldsOrPropertiesExcept("deleted");
		assertThat(new AssetAttachmentEntity()).hasAllNullFieldsOrPropertiesExcept("deleted");
		assertThat(AssetAttachmentEntity.create().isDeleted()).isFalse();
	}

	@Test
	void testPrePersist() {
		final var bean = AssetAttachmentEntity.create();

		bean.prePersist();

		assertThat(bean.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(bean.getUpdated()).isNull();
	}

	@Test
	void testPrePersistMarksAnExistingAssetAsUpdated() {
		final var asset = AssetEntity.create().withCreated(now().minusDays(1));
		final var bean = AssetAttachmentEntity.create().withAsset(asset);

		bean.prePersist();

		assertThat(asset.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void testPrePersistLeavesAnUnpersistedAssetAlone() {
		final var asset = AssetEntity.create();
		final var bean = AssetAttachmentEntity.create().withAsset(asset);

		bean.prePersist();

		assertThat(asset.getUpdated()).isNull();
	}

	@Test
	void testPreUpdate() {
		final var bean = AssetAttachmentEntity.create();

		bean.preUpdate();

		assertThat(bean.getCreated()).isNull();
		assertThat(bean.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}
}
