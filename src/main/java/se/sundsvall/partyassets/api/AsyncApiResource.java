package se.sundsvall.partyassets.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.partyassets.integration.events.AssetEventsAsyncApi;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_YAML_VALUE;

/**
 * Serves the AsyncAPI document of the asset events next to the OpenAPI document. Hidden from the OpenAPI document, so
 * the REST contract does not describe it.
 * <p>
 * The document is written to the response as it is, the way dept44 serves the OpenAPI document. Returned as a String,
 * the YAML message converter would write it as one quoted YAML string.
 */
@RestController
@Hidden
class AsyncApiResource {

	private final AssetEventsAsyncApi asyncApi;

	AsyncApiResource(final AssetEventsAsyncApi asyncApi) {
		this.asyncApi = asyncApi;
	}

	@GetMapping(path = "${springdoc.api-docs.path}/asyncapi.yaml", produces = APPLICATION_YAML_VALUE)
	void asyncApi(final HttpServletResponse response) throws IOException {
		response.setContentType(APPLICATION_YAML_VALUE);
		response.setCharacterEncoding(UTF_8);
		response.getOutputStream().write(asyncApi.yaml().getBytes(UTF_8));
	}
}
