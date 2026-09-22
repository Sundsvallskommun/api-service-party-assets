package se.sundsvall.partyassets.integration.db.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;

class AssetRevisionEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AssetRevisionEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {

		final var id = "id";
		final var assetId = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";
		final var revision = 2;
		final var actor = "joe01doe";
		final var recordedAt = OffsetDateTime.now();
		final var municipalityId = "2281";
		final var origin = "CASEDATA";
		final var externalAssetId = "PRH-0000000001";
		final var partyId = "f2ef7992-7b01-4185-a7f8-cf97dc7f438f";
		final var partyType = "PRIVATE";
		final var type = "PERMIT";
		final var issued = now();
		final var validTo = now().plusYears(1);
		final var replacesId = "0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9";
		final var status = "ACTIVE";
		final var statusReason = "Status reason";
		final var description = "Serveringstillstånd";
		final var additionalParameters = "{\"foo\":\"bar\"}";
		final var caseReferenceIds = "[\"case-1\"]";
		final var jsonParameters = "[{\"key\":\"key1\"}]";
		final var attachments = "[{\"id\":\"attachment-1\"}]";

		final var bean = AssetRevisionEntity.create()
			.withId(id)
			.withAssetId(assetId)
			.withRevision(revision)
			.withActor(actor)
			.withRecordedAt(recordedAt)
			.withMunicipalityId(municipalityId)
			.withOrigin(origin)
			.withExternalAssetId(externalAssetId)
			.withPartyId(partyId)
			.withPartyType(partyType)
			.withType(type)
			.withIssued(issued)
			.withValidTo(validTo)
			.withReplacesId(replacesId)
			.withStatus(status)
			.withStatusReason(statusReason)
			.withDescription(description)
			.withAdditionalParameters(additionalParameters)
			.withCaseReferenceIds(caseReferenceIds)
			.withJsonParameters(jsonParameters)
			.withAttachments(attachments);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getAssetId()).isEqualTo(assetId);
		assertThat(bean.getRevision()).isEqualTo(revision);
		assertThat(bean.getActor()).isEqualTo(actor);
		assertThat(bean.getRecordedAt()).isEqualTo(recordedAt);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getExternalAssetId()).isEqualTo(externalAssetId);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getPartyType()).isEqualTo(partyType);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getIssued()).isEqualTo(issued);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
		assertThat(bean.getReplacesId()).isEqualTo(replacesId);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusReason()).isEqualTo(statusReason);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getAdditionalParameters()).isEqualTo(additionalParameters);
		assertThat(bean.getCaseReferenceIds()).isEqualTo(caseReferenceIds);
		assertThat(bean.getJsonParameters()).isEqualTo(jsonParameters);
		assertThat(bean.getAttachments()).isEqualTo(attachments);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetRevisionEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetRevisionEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPrePersist() {
		final var bean = AssetRevisionEntity.create();

		bean.prePersist();

		assertThat(bean.getRecordedAt()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
	}
}
