package se.sundsvall.partyassets.integration.db.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbBlob;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AssetAttachmentDataEntityTest {

	// The blob carries no value semantics, so it takes no part in identity or in toString.
	private static final String FILE = "file";

	@Test
	void testBean() {
		MatcherAssert.assertThat(AssetAttachmentDataEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToStringExcluding(FILE),
			hasValidBeanEqualsExcluding(FILE),
			hasValidBeanHashCodeExcluding(FILE)));
	}

	@Test
	void testBuilderMethods() {

		final var id = 1L;
		final var file = new MariaDbBlob("file".getBytes());

		final var bean = AssetAttachmentDataEntity.create()
			.withId(id)
			.withFile(file);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getFile()).isSameAs(file);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetAttachmentDataEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetAttachmentDataEntity()).hasAllNullFieldsOrProperties();
	}
}
