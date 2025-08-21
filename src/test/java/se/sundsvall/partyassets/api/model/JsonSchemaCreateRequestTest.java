package se.sundsvall.partyassets.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import org.junit.jupiter.api.Test;

class JsonSchemaCreateRequestTest {

	@Test
	void testBean() {
		assertThat(JsonSchemaCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var description = "description";
		final var name = "name";
		final var value = "value";
		final var version = "version";

		final var bean = JsonSchemaCreateRequest.create()
			.withDescription(description)
			.withName(name)
			.withValue(value)
			.withVersion(version);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getValue()).isEqualTo(value);
		assertThat(bean.getVersion()).isEqualTo(version);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(JsonSchemaCreateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new JsonSchemaCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
