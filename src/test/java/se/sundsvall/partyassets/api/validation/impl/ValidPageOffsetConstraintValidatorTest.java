package se.sundsvall.partyassets.api.validation.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.sundsvall.partyassets.api.model.AssetPagingParameters;
import se.sundsvall.partyassets.api.validation.ValidPageOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ValidPageOffsetConstraintValidatorTest {

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	private final ValidPageOffsetConstraintValidator validator = new ValidPageOffsetConstraintValidator();

	@Test
	void missingParametersAreValid() {
		assertThat(validator.isValid(null, null)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("pagesThatCanBeFetched")
	void pageThatStartsAtAnOffsetThatFitsInAnIntIsValid(final int page, final int limit) {
		assertThat(pageOffsetViolationsOf(page, limit)).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("pagesThatCannotBeFetched")
	void pageThatStartsAtAnOffsetThatDoesNotFitInAnIntIsInvalid(final int page, final int limit, final String message) {
		assertThat(pageOffsetViolationsOf(page, limit)).singleElement().satisfies(violation -> {
			assertThat(violation.getPropertyPath()).hasToString("page");
			assertThat(violation.getMessage()).isEqualTo(message);
		});
	}

	private static Stream<ConstraintViolation<AssetPagingParameters>> pageOffsetViolationsOf(final int page, final int limit) {
		return VALIDATOR.validate(AssetPagingParameters.create().withPage(page).withLimit(limit)).stream()
			.filter(violation -> violation.getConstraintDescriptor().getAnnotation() instanceof ValidPageOffset);
	}

	private static Stream<Arguments> pagesThatCanBeFetched() {
		return Stream.of(
			arguments(1, 1000),
			arguments(2147484, 1000),
			arguments(Integer.MAX_VALUE, 1),
			arguments(2, Integer.MAX_VALUE),
			// Left to the constraints on page and limit
			arguments(0, 1000),
			arguments(Integer.MAX_VALUE, 0),
			arguments(Integer.MAX_VALUE, -1));
	}

	private static Stream<Arguments> pagesThatCannotBeFetched() {
		return Stream.of(
			arguments(2147485, 1000, "must be at most 2147484 when limit is 1000"),
			arguments(Integer.MAX_VALUE, 1000, "must be at most 2147484 when limit is 1000"),
			arguments(Integer.MAX_VALUE, 2, "must be at most 1073741824 when limit is 2"),
			arguments(3, Integer.MAX_VALUE, "must be at most 2 when limit is 2147483647"));
	}
}
