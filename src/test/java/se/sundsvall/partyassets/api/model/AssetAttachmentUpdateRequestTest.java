package se.sundsvall.partyassets.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AssetAttachmentUpdateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(AssetAttachmentUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {

		final var fileName = "lokalritning.pdf";
		final var category = "LOKALRITNING";
		final var description = "description";

		final var bean = AssetAttachmentUpdateRequest.create()
			.withFileName(fileName)
			.withCategory(category)
			.withDescription(description);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getFileName()).isEqualTo(fileName);
		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getDescription()).isEqualTo(description);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AssetAttachmentUpdateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new AssetAttachmentUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
