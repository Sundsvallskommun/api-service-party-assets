package se.sundsvall.partyassets.api.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AssetRevisionTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AssetRevision.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var id = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0";
		final var revision = 2;
		final var actor = "joe01doe";
		final var recordedAt = OffsetDateTime.now();
		final var assetId = "PRH-123456789";
		final var origin = "CASEDATA";
		final var partyId = "123e4567-e89b-12d3-a456-426614174000";
		final var type = "PERMIT";
		final var issued = LocalDate.of(2021, 1, 1);
		final var validTo = LocalDate.of(2021, 12, 31);
		final var status = Status.ACTIVE;
		final var statusReason = "Status reason";
		final var description = "Asset description";
		final var additionalParameters = Map.of("foo", "bar");
		final var jsonParameters = List.of(AssetJsonParameter.create().withKey("key1"));
		final var replacesId = "0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9";
		final var attachments = List.of(AssetAttachment.create().withId("attachment-1"));

		final var bean = AssetRevision.create()
			.withId(id)
			.withRevision(revision)
			.withActor(actor)
			.withRecordedAt(recordedAt)
			.withAssetId(assetId)
			.withOrigin(origin)
			.withPartyId(partyId)
			.withType(type)
			.withIssued(issued)
			.withValidTo(validTo)
			.withStatus(status)
			.withStatusReason(statusReason)
			.withDescription(description)
			.withAdditionalParameters(additionalParameters)
			.withJsonParameters(jsonParameters)
			.withReplacesId(replacesId)
			.withAttachments(attachments);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getRevision()).isEqualTo(revision);
		assertThat(bean.getActor()).isEqualTo(actor);
		assertThat(bean.getRecordedAt()).isEqualTo(recordedAt);
		assertThat(bean.getAssetId()).isEqualTo(assetId);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getIssued()).isEqualTo(issued);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusReason()).isEqualTo(statusReason);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getAdditionalParameters()).isEqualTo(additionalParameters);
		assertThat(bean.getJsonParameters()).isEqualTo(jsonParameters);
		assertThat(bean.getReplacesId()).isEqualTo(replacesId);
		assertThat(bean.getAttachments()).isEqualTo(attachments);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetRevision.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetRevision()).hasAllNullFieldsOrProperties();
	}
}
