package se.sundsvall.citizenassets.api.validation.impl;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import se.sundsvall.citizenassets.api.model.AssetCreateRequest;
import se.sundsvall.citizenassets.api.model.Status;

@ExtendWith(MockitoExtension.class)
class ValidStatusReasonOnCreateConstraintValidatorTest {
	private static final String ERROR_MESSAGE = "'%s' is not valid reason for status %s. Valid reasons are %s.";

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@Mock
	private ConstraintViolationBuilder constraintViolationBuilderMock;

	@InjectMocks
	private ValidStatusReasonOnCreateConstraintValidator validator;

	@ParameterizedTest
	@MethodSource("validReasonStatusesProvider")
	void testValidReasonStatuses(Status status, String statusReason, List<String> validReasons) {
		assertThat(validator.isValid(AssetCreateRequest.create().withStatus(status).withStatusReason(statusReason), constraintValidatorContextMock)).isTrue();

		verifyNoInteractions(constraintValidatorContextMock);
	}

	@ParameterizedTest
	@MethodSource("invalidReasonStatusesProvider")
	void testInvalidReasonStatuses(Status status, String statusReason, List<String> validReasons) {
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

		assertThat(validator.isValid(AssetCreateRequest.create().withStatus(status).withStatusReason(statusReason), constraintValidatorContextMock)).isFalse();

		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate(ERROR_MESSAGE.formatted(statusReason, status, validReasons));
		verify(constraintViolationBuilderMock).addConstraintViolation();
	}

	private static Stream<Arguments> validReasonStatusesProvider() {
		return Stream.of(
			Arguments.of(null, null, emptyList()),
			Arguments.of(Status.ACTIVE, null, emptyList()),
			Arguments.of(Status.BLOCKED, "IRREGULARITY", List.of("IRREGULARITY", "LOST")),
			Arguments.of(Status.BLOCKED, "LOST", List.of("IRREGULARITY", "LOST")),
			Arguments.of(Status.EXPIRED, null, emptyList()));
	}

	private static Stream<Arguments> invalidReasonStatusesProvider() {
		return Stream.of(
			Arguments.of(Status.ACTIVE, "", emptyList()),
			Arguments.of(Status.ACTIVE, " ", emptyList()),
			Arguments.of(Status.ACTIVE, "SOME_VALUE", emptyList()),
			Arguments.of(Status.BLOCKED, null, List.of("IRREGULARITY", "LOST")),
			Arguments.of(Status.BLOCKED, "", List.of("IRREGULARITY", "LOST")),
			Arguments.of(Status.BLOCKED, " ", List.of("IRREGULARITY", "LOST")),
			Arguments.of(Status.BLOCKED, "SOME_VALUE", List.of("IRREGULARITY", "LOST")),
			Arguments.of(Status.EXPIRED, "", emptyList()),
			Arguments.of(Status.EXPIRED, " ", emptyList()),
			Arguments.of(Status.EXPIRED, "SOME_VALUE", emptyList()));
	}
}
