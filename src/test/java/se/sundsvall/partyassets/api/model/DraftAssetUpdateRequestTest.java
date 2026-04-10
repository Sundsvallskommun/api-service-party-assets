package se.sundsvall.partyassets.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class DraftAssetUpdateRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		assertThat(DraftAssetUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var additionalParameters = Map.of("key", "value");
		final var jsonParameters = List.of(AssetJsonParameter.create());
		final var status = Status.ACTIVE;
		final var statusReason = "statusReason";
		final var validTo = LocalDate.now();

		final var draftAssetUpdateRequest = new DraftAssetUpdateRequest();
		draftAssetUpdateRequest.setAdditionalParameters(additionalParameters);
		draftAssetUpdateRequest.setJsonParameters(jsonParameters);
		draftAssetUpdateRequest.setStatus(status);
		draftAssetUpdateRequest.setStatusReason(statusReason);
		draftAssetUpdateRequest.setValidTo(validTo);

		assertThat(draftAssetUpdateRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(draftAssetUpdateRequest.getAdditionalParameters()).isEqualTo(additionalParameters);
		assertThat(draftAssetUpdateRequest.getJsonParameters()).isEqualTo(jsonParameters);
		assertThat(draftAssetUpdateRequest.getStatus()).isEqualTo(status);
		assertThat(draftAssetUpdateRequest.getStatusReason()).isEqualTo(statusReason);
		assertThat(draftAssetUpdateRequest.getValidTo()).isEqualTo(validTo);

		final var beanWithValidTo = new DraftAssetUpdateRequest();
		beanWithValidTo.withValidTo(validTo);
		assertThat(beanWithValidTo.getValidTo()).isEqualTo(validTo);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new DraftAssetUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
