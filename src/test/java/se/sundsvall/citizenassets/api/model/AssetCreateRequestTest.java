package se.sundsvall.citizenassets.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AssetCreateRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		assertThat(AssetCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var additionalParameters = Map.of("key", "value");
		final var assetId = "assetId";
		final var caseReferenceIds = List.of("entry");
		final var description = "description";
		final var issued = LocalDate.now();
		final var partyId = "partyId";
		final var status = Status.ACTIVE;
		final var statusReason = "statusReason";
		final var type = "type";
		final var validTo = LocalDate.now();

		final var bean = AssetCreateRequest.create()
			.withAdditionalParameters(additionalParameters)
			.withAssetId(assetId)
			.withCaseReferenceIds(caseReferenceIds)
			.withDescription(description)
			.withIssued(issued)
			.withPartyId(partyId)
			.withStatus(status)
			.withStatusReason(statusReason)
			.withType(type)
			.withValidTo(validTo);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAdditionalParameters()).isEqualTo(additionalParameters);
		assertThat(bean.getAssetId()).isEqualTo(assetId);
		assertThat(bean.getCaseReferenceIds()).isEqualTo(caseReferenceIds);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getIssued()).isEqualTo(issued);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusReason()).isEqualTo(statusReason);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetCreateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
