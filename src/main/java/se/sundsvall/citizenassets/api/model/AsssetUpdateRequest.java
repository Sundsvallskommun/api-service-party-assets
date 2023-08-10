package se.sundsvall.citizenassets.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
public class AsssetUpdateRequest {

	@Schema(description = "Case reference ids", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
	private List<String> caseReferenceIds;

	@Schema(description = "Valid to date", example = "2021-12-31")
	private LocalDate validTo;

	@Schema(description = "Asset status", example = "ACTIVE")
	private Status status;

	@Schema(description = "Additional parameters", example = "{\"foo\":\"bar\"}")
	private Map<String, String> additionalParameters;
}
