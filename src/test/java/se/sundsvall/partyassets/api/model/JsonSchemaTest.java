package se.sundsvall.partyassets.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JsonSchemaTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(JsonSchema.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var created = OffsetDateTime.now();
		final var description = "description";
		final var id = "id";
		final var name = "name";
		final var numberOfReferences = 1L;
		final var value = "value";
		final var version = "version";

		final var bean = JsonSchema.create()
			.withCreated(created)
			.withDescription(description)
			.withId(id)
			.withName(name)
			.withNumberOfReferences(numberOfReferences)
			.withValue(value)
			.withVersion(version);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNumberOfReferences()).isEqualTo(numberOfReferences);
		assertThat(bean.getValue()).isEqualTo(value);
		assertThat(bean.getVersion()).isEqualTo(version);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(JsonSchema.create()).hasAllNullFieldsOrPropertiesExcept("numberOfReferences");
		assertThat(new JsonSchema()).hasAllNullFieldsOrPropertiesExcept("numberOfReferences");
	}
}
