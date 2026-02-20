package se.sundsvall.partyassets.integration.db.model;

import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class StatusEntityIdTest {

	@Test
	void testBean() {
		assertThat(StatusEntityId.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals()));
	}

	@Test
	void testConstructorWithArgs() {
		final var name = "BLOCKED";
		final var municipalityId = "2281";

		final var bean = new StatusEntityId(name, municipalityId);

		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new StatusEntityId()).hasAllNullFieldsOrProperties();
	}

}
