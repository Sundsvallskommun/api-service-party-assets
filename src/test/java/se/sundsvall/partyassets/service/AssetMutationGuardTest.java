package se.sundsvall.partyassets.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;
import se.sundsvall.partyassets.scheduler.AssetExpirationWorker;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class AssetMutationGuardTest {

	private static final Set<String> ASSET_SERVICE_METHODS = Set.of(
		"getAssets(String,AssetSearchRequest)",
		"getDraftAssets(String,AssetSearchRequest)",
		"getAsset(String,String)",
		"createAsset(String,AssetCreateRequest,String)",
		"deleteAsset(String,String)",
		"copyAsset(String,String)",
		"updateAsset(String,String,DraftAssetUpdateRequest,String)",
		"updateAsset(String,String,AssetUpdateRequest,String)");

	private static final Set<String> ASSET_ATTACHMENT_SERVICE_METHODS = Set.of(
		"createAttachment(String,String,MultipartFile,String,String)",
		"readAttachments(String,String)",
		"readAttachment(String,String,String)",
		"updateAttachment(String,String,String,AssetAttachmentUpdateRequest)",
		"deleteAttachment(String,String,String)");

	private static final Set<String> ASSET_EXPIRATION_WORKER_METHODS = Set.of(
		"findExpirableAssetIds()",
		"expire(String)");

	private static final String MESSAGE = """
		The public API of %s has changed. Every method that changes an asset has to snapshot it before it mutates, or \
		that change is missing from the history and nothing reveals it. Decide whether the new method mutates, take a \
		snapshot if it does, then list its signature here. See docs/design-revisionshantering.md.""";

	@Test
	void assetServiceHasNoUnreviewedMethods() {
		assertThat(signaturesOf(AssetService.class))
			.as(MESSAGE.formatted(AssetService.class.getSimpleName()))
			.isEqualTo(ASSET_SERVICE_METHODS);
	}

	@Test
	void assetAttachmentServiceHasNoUnreviewedMethods() {
		assertThat(signaturesOf(AssetAttachmentService.class))
			.as(MESSAGE.formatted(AssetAttachmentService.class.getSimpleName()))
			.isEqualTo(ASSET_ATTACHMENT_SERVICE_METHODS);
	}

	@Test
	void assetExpirationWorkerHasNoUnreviewedMethods() {
		assertThat(signaturesOf(AssetExpirationWorker.class))
			.as(MESSAGE.formatted(AssetExpirationWorker.class.getSimpleName()))
			.isEqualTo(ASSET_EXPIRATION_WORKER_METHODS);
	}

	private static Set<String> signaturesOf(final Class<?> type) {
		return Arrays.stream(type.getDeclaredMethods())
			.filter(method -> isPublic(method.getModifiers()))
			.filter(method -> !isStatic(method.getModifiers()))
			.filter(method -> !method.isSynthetic())
			.map(AssetMutationGuardTest::signatureOf)
			.collect(toSet());
	}

	private static String signatureOf(final Method method) {
		return Arrays.stream(method.getParameterTypes())
			.map(Class::getSimpleName)
			.collect(joining(",", method.getName() + "(", ")"));
	}
}
