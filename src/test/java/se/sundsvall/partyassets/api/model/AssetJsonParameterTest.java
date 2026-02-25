package se.sundsvall.partyassets.api.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class AssetJsonParameterTest {

	@Test
	void testBean() {
		assertThat(AssetJsonParameter.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var key = "key";
		final var value = new ObjectMapper().createObjectNode().put("test", "value");
		final var schemaId = "schemaId";

		final var bean = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(value);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getKey()).isEqualTo(key);
		assertThat(bean.getSchemaId()).isEqualTo(schemaId);
		assertThat(bean.getValue()).isEqualTo(value);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetJsonParameter.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetJsonParameter()).hasAllNullFieldsOrProperties();
	}
}
