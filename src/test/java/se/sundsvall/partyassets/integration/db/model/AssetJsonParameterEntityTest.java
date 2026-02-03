package se.sundsvall.partyassets.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AssetJsonParameterEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AssetJsonParameterEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToStringExcluding("asset"),
			hasValidBeanEqualsExcluding("asset"),
			hasValidBeanHashCodeExcluding("asset")));
	}

	@Test
	void testBuilderMethods() {

		final var asset = AssetEntity.create();
		final var id = "id";
		final var key = "key";
		final var schemaId = "schemaId";
		final var value = "value";

		final var bean = AssetJsonParameterEntity.create()
			.withAsset(asset)
			.withId(id)
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(value);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAsset()).isEqualTo(asset);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getKey()).isEqualTo(key);
		assertThat(bean.getSchemaId()).isEqualTo(schemaId);
		assertThat(bean.getValue()).isEqualTo(value);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetJsonParameterEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetJsonParameterEntity()).hasAllNullFieldsOrProperties();
	}
}
