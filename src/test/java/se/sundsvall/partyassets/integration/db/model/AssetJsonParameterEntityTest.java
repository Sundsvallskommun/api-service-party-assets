package se.sundsvall.partyassets.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AssetJsonParameterEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(AssetJsonParameterEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var asset = AssetEntity.create();
		final var id = "id";
		final var key = "key";
		final var schema = JsonSchemaEntity.create();
		final var value = "value";

		final var bean = AssetJsonParameterEntity.create()
			.withAsset(asset)
			.withId(id)
			.withKey(key)
			.withSchema(schema)
			.withValue(value);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAsset()).isEqualTo(asset);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getKey()).isEqualTo(key);
		assertThat(bean.getSchema()).isEqualTo(schema);
		assertThat(bean.getValue()).isEqualTo(value);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetJsonParameterEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetJsonParameterEntity()).hasAllNullFieldsOrProperties();
	}
}
