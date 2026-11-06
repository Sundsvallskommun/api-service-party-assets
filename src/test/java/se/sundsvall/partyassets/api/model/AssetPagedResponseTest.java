package se.sundsvall.partyassets.api.model;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class AssetPagedResponseTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> PagingAndSortingMetaData.create().withPage(new Random().nextInt()), PagingAndSortingMetaData.class);
	}

	@Test
	void testBean() {
		assertThat(AssetPagedResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var assets = List.of(Asset.create().withId("id"));
		final var metaData = PagingAndSortingMetaData.create().withPage(2);

		final var bean = AssetPagedResponse.create()
			.withAssets(assets)
			.withMetaData(metaData);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAssets()).isEqualTo(assets);
		assertThat(bean.getMetaData()).isEqualTo(metaData);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetPagedResponse.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetPagedResponse()).hasAllNullFieldsOrProperties();
	}
}
