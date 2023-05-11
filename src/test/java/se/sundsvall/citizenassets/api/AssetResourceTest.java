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

import se.sundsvall.citizenassets.Application;
import se.sundsvall.citizenassets.TestFactory;
import se.sundsvall.citizenassets.api.model.Asset;
import se.sundsvall.citizenassets.api.model.AssetRequest;
import se.sundsvall.citizenassets.service.AssetService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssetResourceTest {

    @MockBean
    private AssetService mockAssetService;
    @Autowired
    private WebTestClient webTestClient;


    @Test
    void getAsset() {
        var asset = TestFactory.getAsset();
        var uuid = UUID.randomUUID();

        when(mockAssetService.getAsset(any(UUID.class))).thenReturn(asset);

        var result = webTestClient.get()
            .uri("/assets/{partyId}", uuid)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(APPLICATION_JSON)
            .expectBody(Asset.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).usingRecursiveComparison().isEqualTo(asset);


        verify(mockAssetService, times(1)).getAsset(any());
        verifyNoMoreInteractions(mockAssetService);
    }


    @ParameterizedTest
    @ValueSource(strings = {"imNotARealUUID", "1", "1234-1234-1234-1234"})
    void getAsset_faultyPartyId(String uuid) {
        var result = webTestClient.get()
            .uri("/assets/{partyId}", uuid)
            .exchange()
            .expectStatus()
            .is4xxClientError()
            .expectBody(Problem.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isNotNull();
        assertThat(result.getStatus().getStatusCode()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: " + uuid);

        verifyNoInteractions(mockAssetService);
    }


    @Test
    void getAssets() {
        var assets = List.of(TestFactory.getAsset());
        when(mockAssetService.getAssets(any(AssetRequest.class))).thenReturn(assets);
        var result = webTestClient.get()
            .uri("/assets")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(APPLICATION_JSON)
            .expectBodyList(Asset.class)
            .returnResult()
            .getResponseBody();

        assertThat(result).usingRecursiveComparison().isEqualTo(assets);

        verify(mockAssetService, times(1)).getAssets(any(AssetRequest.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @ParameterizedTest
    @ValueSource(strings = {"imNotARealUUID", "1", "1234-1234-1234-1234"})
    void getAssets_faultyPartyId(String uuid) {

        var test = webTestClient.get()
            .uri("/assets/{partyId}", uuid)
            .exchange()
            .expectStatus()
            .is4xxClientError()
            .expectBody(Problem.class)
            .returnResult()
            .getResponseBody();

        assertThat(test).isNotNull();
        assertThat(test.getStatus()).isNotNull();
        assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
        assertThat(test.getDetail()).isEqualTo("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: " + uuid);
        assertThat(test.getTitle()).isEqualTo("Bad Request");
        verifyNoInteractions(mockAssetService);
    }


    @Test
    void createAsset(@Value("${local.server.port}") int myPort) {

        var uuid = UUID.randomUUID();
        var assetRequest = TestFactory.getAssetRequest(uuid);

        when(mockAssetService.createAsset(any(AssetRequest.class))).thenReturn(uuid.toString());
        webTestClient.post()
            .uri("/assets")
            .bodyValue(assetRequest)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectHeader()
            .location("http://localhost:" + myPort + "/asset/" + uuid);


        verify(mockAssetService, times(1)).createAsset(any(AssetRequest.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @Test
    void testUpdateAsset() {
        var uuid = UUID.randomUUID();
        var assetRequest = TestFactory.getAssetRequest(uuid);

        webTestClient.put()
            .uri("/assets/{partyId}", uuid)
            .bodyValue(assetRequest)
            .exchange()
            .expectStatus()
            .isNoContent();

        verify(mockAssetService, times(1))
            .updateAsset(any(UUID.class), any(AssetRequest.class));
        verifyNoMoreInteractions(mockAssetService);
    }


    @Test
    void testUpdateAsset_faulytUUID() {
        var uuid = "imNotARealUUID";
        var assetRequest = TestFactory.getAssetRequest(UUID.randomUUID());
        var test = webTestClient.put()
            .uri("/assets/{partyId}", uuid)
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