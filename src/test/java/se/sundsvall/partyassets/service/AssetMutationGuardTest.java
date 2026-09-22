package se.sundsvall.partyassets.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Freezes the public surface of the two services that may change an asset. Nothing enforces the snapshot rule at
 * runtime: it is written explicitly in the service, because a Hibernate event listener would hide it from whoever reads
 * the code (docs/design-revisionshantering.md). This test is the substitute.
 */
class AssetMutationGuardTest {

	private static final Set<String> ASSET_SERVICE_METHODS = Set.of(
		"getAssets(String,AssetSearchRequest)",
		"getDraftAssets(String,AssetSearchRequest)",
		"getAsset(String,String)",
		"createAsset(String,AssetCreateRequest,String)",
		"deleteAsset(String,String)",
		"copyAsset(String,String)",
		"updateAsset(String,String,DraftAssetUpdateRequest)",
		"updateAsset(String,String,AssetUpdateRequest)");

	private static final Set<String> ASSET_ATTACHMENT_SERVICE_METHODS = Set.of(
		"createAttachment(String,String,MultipartFile,String,String)",
		"readAttachments(String,String)",
		"readAttachment(String,String,String)",
		"updateAttachment(String,String,String,AssetAttachmentUpdateRequest)",
		"deleteAttachment(String,String,String)");

	private static final String MESSAGE = """
		The public API of %s has changed. Every method that changes an asset has to record a revision before it mutates, \
		or the revision number is bumped without a snapshot and the numbering gets a gap. Decide whether the new method \
		mutates, call recordRevision(..) if it does, then list its signature here. \
		See docs/design-revisionshantering.md.""";

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

	// Full signatures rather than bare names: with names alone a new overload of an existing method would slip past,
	// which is the change this test exists to catch. Synthetic methods are skipped because JaCoCo adds one under
	// coverage runs but not under a plain test run.
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
