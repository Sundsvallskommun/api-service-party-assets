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

import java.time.OffsetDateTime;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JsonSchemaEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(JsonSchemaEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var created = now().minusDays(1);
		final var description = "description";
		final var id = "id";
		final var municipalityId = "municipalityId";
		final var name = "name";
		final var value = "value";
		final var version = "version";

		final var bean = JsonSchemaEntity.create()
			.withCreated(created)
			.withDescription(description)
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withName(name)
			.withValue(value)
			.withVersion(version);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getValue()).isEqualTo(value);
		assertThat(bean.getVersion()).isEqualTo(version);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(JsonSchemaEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new JsonSchemaEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPrePersist() {
		final var bean = JsonSchemaEntity.create();

		bean.prePersist();

		assertThat(bean.getCreated()).isCloseTo(now(), within(2, SECONDS));
	}
}
