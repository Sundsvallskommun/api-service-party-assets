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

class AssetUpdateRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		assertThat(AssetUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var status = Status.ACTIVE;
		final var statusReason = "someReason";
		final var validTo = LocalDate.now();
		final var indefinitely = true;
		final var title = "title";
		final var additionalParameters = Map.of("key", "value");
		final var jsonParameters = List.of(AssetJsonParameter.create().withKey("key"));

		final var assetUpdateRequest = AssetUpdateRequest.create()
			.withStatus(status)
			.withStatusReason(statusReason)
			.withValidTo(validTo)
			.withIndefinitely(indefinitely)
			.withTitle(title)
			.withAdditionalParameters(additionalParameters)
			.withJsonParameters(jsonParameters);

		assertThat(assetUpdateRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(assetUpdateRequest.getStatus()).isEqualTo(status);
		assertThat(assetUpdateRequest.getStatusReason()).isEqualTo(statusReason);
		assertThat(assetUpdateRequest.getValidTo()).isEqualTo(validTo);
		assertThat(assetUpdateRequest.getIndefinitely()).isEqualTo(indefinitely);
		assertThat(assetUpdateRequest.getTitle()).isEqualTo(title);
		assertThat(assetUpdateRequest.getAdditionalParameters()).isEqualTo(additionalParameters);
		assertThat(assetUpdateRequest.getJsonParameters()).isEqualTo(jsonParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new AssetUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
