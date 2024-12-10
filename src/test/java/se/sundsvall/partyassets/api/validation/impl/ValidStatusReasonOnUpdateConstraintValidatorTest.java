package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.StatusService;

@ExtendWith(MockitoExtension.class)
class ValidStatusReasonOnUpdateConstraintValidatorTest {

	private static final String MUNICIPALITY_ID = "2281";

	static final String PATH_VARIABLE_MUNICIPALITY_ID = "municipalityId";

	private static final String ERROR_MESSAGE = "'%s' is not valid reason for status %s. Valid reasons are %s.";

	private static final Map<Status, List<String>> VALID_STATUS_REASONS_FOR_STATUSES = Map.of(
		Status.BLOCKED, List.of("IRREGULARITY", "LOST"));

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@Mock
	private ConstraintViolationBuilder constraintViolationBuilderMock;

	@Mock
	private StatusService statusServiceMock;

	@Mock
	private RequestAttributes requestAttributesMock;

	@InjectMocks
	private ValidStatusReasonOnUpdateConstraintValidator validator;

	@ParameterizedTest
	@MethodSource("validReasonStatusesProvider")
	void testValidReasonStatuses(final Status status, final String statusReason, final List<String> validReasons) {

		final var attributes = Map.of(PATH_VARIABLE_MUNICIPALITY_ID, MUNICIPALITY_ID);

		try (final var requestContextHolderMock = Mockito.mockStatic(RequestContextHolder.class)) {
			requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributesMock);
			lenient().when(requestAttributesMock.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST)).thenReturn(attributes);
			lenient().when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);

			assertThat(validator.isValid(AssetUpdateRequest.create().withStatus(status).withStatusReason(statusReason), constraintValidatorContextMock)).isTrue();

			verifyNoInteractions(constraintValidatorContextMock);

		}
	}

	@ParameterizedTest
	@MethodSource("invalidReasonStatusesProvider")
	void testInvalidReasonStatuses(final Status status, final String statusReason, final List<String> validReasons) {

		final var attributes = Map.of(PATH_VARIABLE_MUNICIPALITY_ID, MUNICIPALITY_ID);

		try (final var requestContextHolderMock = Mockito.mockStatic(RequestContextHolder.class)) {
			requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributesMock);
			lenient().when(requestAttributesMock.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST)).thenReturn(attributes);
			when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);
			when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

			assertThat(validator.isValid(AssetUpdateRequest.create().withStatus(status).withStatusReason(statusReason), constraintValidatorContextMock)).isFalse();

			verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
			verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate(ERROR_MESSAGE.formatted(statusReason, status, validReasons));
			verify(constraintViolationBuilderMock).addConstraintViolation();

		}
	}

	private static Stream<Arguments> validReasonStatusesProvider() {
		final var validBlockedReasons = List.of("IRREGULARITY", "LOST");

		return Stream.of(
			Arguments.of(null, null, emptyList()),
			Arguments.of(Status.ACTIVE, null, emptyList()),
			Arguments.of(Status.BLOCKED, "IRREGULARITY", validBlockedReasons),
			Arguments.of(Status.BLOCKED, "LOST", validBlockedReasons),
			Arguments.of(Status.EXPIRED, null, emptyList()));
	}

	private static Stream<Arguments> invalidReasonStatusesProvider() {
		final var validBlockedReasons = List.of("IRREGULARITY", "LOST");

		return Stream.of(
			Arguments.of(Status.ACTIVE, "", emptyList()),
			Arguments.of(Status.ACTIVE, " ", emptyList()),
			Arguments.of(Status.ACTIVE, "SOME_VALUE", emptyList()),
			Arguments.of(Status.BLOCKED, null, validBlockedReasons),
			Arguments.of(Status.BLOCKED, "", validBlockedReasons),
			Arguments.of(Status.BLOCKED, " ", validBlockedReasons),
			Arguments.of(Status.BLOCKED, "SOME_VALUE", validBlockedReasons),
			Arguments.of(Status.EXPIRED, "", emptyList()),
			Arguments.of(Status.EXPIRED, " ", emptyList()),
			Arguments.of(Status.EXPIRED, "SOME_VALUE", emptyList()));
	}
}
