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
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StatusEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(StatusEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var created = OffsetDateTime.now().minusDays(1);
		final var name = "name";
		final var municipalityId = "municipalityId";
		final var reasons = List.of("reason1", "reason2");
		final var updated = OffsetDateTime.now();

		final var bean = StatusEntity.create()
			.withCreated(created)
			.withName(name)
			.withMunicipalityId(municipalityId)
			.withReasons(reasons)
			.withUpdated(updated);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getReasons()).isEqualTo(reasons);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(StatusEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new StatusEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPrePersist() {
		final var bean = StatusEntity.create();

		bean.prePersist();

		assertThat(bean.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(bean.getUpdated()).isNull();
	}

	@Test
	void testPreUpdate() {
		final var bean = StatusEntity.create();

		bean.preUpdate();

		assertThat(bean.getCreated()).isNull();
		assertThat(bean.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

}
