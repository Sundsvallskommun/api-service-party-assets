package se.sundsvall.partyassets.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.partyassets.api.model.Status;

class AssetEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		assertThat(AssetEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var additionParameters = Map.of("key", "value");
		final var assetId = "assetId";
		final var caseReferenceIds = List.of("entry");
		final var created = OffsetDateTime.now().minusDays(1);
		final var description = "description";
		final var id = "id";
		final var issued = LocalDate.now();
		final var origin = "origin";
		final var partyId = "partyId";
		final var partyType = PartyType.ENTERPRISE;
		final var status = Status.ACTIVE;
		final var statusReason = "statusReason";
		final var type = "type";
		final var updated = OffsetDateTime.now();
		final var validTo = LocalDate.now();

		final var bean = AssetEntity.create()
			.withAdditionalParameters(additionParameters)
			.withAssetId(assetId)
			.withCaseReferenceIds(caseReferenceIds)
			.withCreated(created)
			.withDescription(description)
			.withId(id)
			.withIssued(issued)
			.withOrigin(origin)
			.withPartyId(partyId)
			.withPartyType(partyType)
			.withStatus(status)
			.withStatusReason(statusReason)
			.withType(type)
			.withUpdated(updated)
			.withValidTo(validTo);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAdditionalParameters()).isEqualTo(additionParameters);
		assertThat(bean.getAssetId()).isEqualTo(assetId);
		assertThat(bean.getCaseReferenceIds()).isEqualTo(caseReferenceIds);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getIssued()).isEqualTo(issued);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getPartyType()).isEqualTo(partyType);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusReason()).isEqualTo(statusReason);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPrePersist() {
		final var bean = AssetEntity.create();

		bean.prePersist();

		assertThat(bean.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(bean.getUpdated()).isNull();
	}

	@Test
	void testPreUpdate() {
		final var bean = AssetEntity.create();

		bean.preUpdate();

		assertThat(bean.getCreated()).isNull();
		assertThat(bean.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}
}
