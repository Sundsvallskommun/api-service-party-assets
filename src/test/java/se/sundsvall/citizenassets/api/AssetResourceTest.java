package se.sundsvall.citizenassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.citizenassets.Application;
import se.sundsvall.citizenassets.TestFactory;
import se.sundsvall.citizenassets.api.model.Asset;
import se.sundsvall.citizenassets.api.model.AssetCreateRequest;
import se.sundsvall.citizenassets.api.model.AssetSearchRequest;
import se.sundsvall.citizenassets.api.model.AsssetUpdateRequest;
import se.sundsvall.citizenassets.api.model.Status;
import se.sundsvall.citizenassets.service.AssetService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssetResourceTest {

    @MockBean
    private AssetService mockAssetService;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getAssets() {
        var assets = List.of(TestFactory.getAsset());
        when(mockAssetService.getAssets(any(AssetSearchRequest.class))).thenReturn(assets);
        var result = webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path("/assets")
                .queryParam("partyId", UUID.randomUUID())
                .queryParam("assetId", "assetId")
                .queryParam("status", Status.ACTIVE)
                .queryParam("type", "PERMIT")
                .queryParam("issued", "2020-01-01")
                .queryParam("validTo", "2020-01-01")
                .queryParam("description", "description")
                .queryParam("caseReferenceIds", UUID.randomUUID())
                .build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(APPLICATION_JSON)
            .expectBodyList(Asset.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).usingRecursiveComparison().isEqualTo(assets);

        verify(mockAssetService, times(1)).getAssets(any(AssetSearchRequest.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @ParameterizedTest
    @ValueSource(strings = {"imNotARealUUID", "1", "1234-1234-1234-1234"})
    void getAssets_faultyPartyId(String uuid) {

        var test = webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path("/assets")
                .queryParam("partyId", uuid)
                .build())
            .exchange()
            .expectStatus()
            .is4xxClientError()
            .expectBody(ConstraintViolationProblem.class)
            .returnResult()
            .getResponseBody();

        assertThat(test).isNotNull();
        assertThat(test.getStatus()).isNotNull();
        assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
        assertThat(test.getViolations().get(0).getMessage()).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: " + uuid);
        assertThat(test.getViolations().get(0).getField()).isEqualTo("partyId");
        assertThat(test.getTitle()).isEqualTo("Constraint Violation");
        assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
        verifyNoInteractions(mockAssetService);
    }


    @Test
    void createAsset(@Value("${local.server.port}") int myPort) {

        var uuid = UUID.randomUUID();
        var assetRequest = TestFactory.getAssetCreateRequest(uuid);

        when(mockAssetService.createAsset(any(AssetCreateRequest.class))).thenReturn(uuid.toString());
        webTestClient.post()
            .uri("/assets")
            .bodyValue(assetRequest)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectHeader()
            .location("http://localhost:" + myPort + "/asset/" + uuid);


        verify(mockAssetService, times(1)).createAsset(any(AssetCreateRequest.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @Test
    void testUpdateAsset() {
        var uuid = UUID.randomUUID();
        var assetRequest = TestFactory.getAsssetUpdateRequest();

        webTestClient.put()
            .uri("/assets/{partyId}/{assetId}", uuid, "assetId")
            .bodyValue(assetRequest)
            .exchange()
            .expectStatus()
            .isNoContent();

        verify(mockAssetService, times(1))
            .updateAsset(any(UUID.class), any(String.class),any(AsssetUpdateRequest.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @Test
    void testUpdateAsset_faulytUUID() {
        var uuid = "imNotARealUUID";
        var assetRequest = TestFactory.getAsssetUpdateRequest();
        var test = webTestClient.put()
            .uri("/assets/{partyId}/{assetId}", uuid, "assetId")
            .bodyValue(assetRequest)
            .exchange()
            .expectStatus()
            .is4xxClientError()
            .expectBody(Problem.class).returnResult().getResponseBody();

        assertThat(test).isNotNull();
        assertThat(test.getStatus()).isNotNull();
        assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
        assertThat(test.getDetail()).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: imNotARealUUID");
        assertThat(test.getTitle()).isEqualTo("Bad Request");
        verifyNoInteractions(mockAssetService);
    }


    @Test
    void testDeleteAsset() {
        var uuid = UUID.randomUUID();
        webTestClient.delete()
            .uri("/assets/{partyId}", uuid)
            .exchange()
            .expectStatus()
            .isNoContent();

        verify(mockAssetService, times(1)).deleteAsset(any(UUID.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @Test
    void testDeleteAssert_faultyUUID() {
        var uuid = "imNotARealUUID";
        var test = webTestClient.delete()
            .uri("/assets/{partyId}", uuid)
            .exchange()
            .expectStatus()
            .is4xxClientError()
            .expectBody(Problem.class).returnResult().getResponseBody();

        assertThat(test).isNotNull();
        assertThat(test.getStatus()).isNotNull();
        assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
        assertThat(test.getDetail()).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: imNotARealUUID");
        assertThat(test.getTitle()).isEqualTo("Bad Request");
        verifyNoInteractions(mockAssetService);

    }


}