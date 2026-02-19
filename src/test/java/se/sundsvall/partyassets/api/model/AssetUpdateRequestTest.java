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
		final var additionalParameters = Map.of("key", "value");
		final var jsonParameters = List.of(AssetJsonParameter.create());
		final var status = Status.ACTIVE;
		final var statusReason = "statusReason";
		final var validTo = LocalDate.now();

		final var bean = AssetUpdateRequest.create()
			.withAdditionalParameters(additionalParameters)
			.withJsonParameters(jsonParameters)
			.withStatus(status)
			.withStatusReason(statusReason)
			.withValidTo(validTo);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAdditionalParameters()).isEqualTo(additionalParameters);
		assertThat(bean.getJsonParameters()).isEqualTo(jsonParameters);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusReason()).isEqualTo(statusReason);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetUpdateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
