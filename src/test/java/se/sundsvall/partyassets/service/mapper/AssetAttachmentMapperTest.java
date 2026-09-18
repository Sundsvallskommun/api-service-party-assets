package se.sundsvall.partyassets.service.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mariadb.jdbc.MariaDbBlob;
import org.springframework.mock.web.MockMultipartFile;
import se.sundsvall.partyassets.api.model.AssetAttachmentUpdateRequest;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentDataEntity;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.copyAssetAttachmentData;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachment;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachmentEntity;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.toAssetAttachments;
import static se.sundsvall.partyassets.service.mapper.AssetAttachmentMapper.updateEntity;

class AssetAttachmentMapperTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final byte[] CONTENT = "content".getBytes();

	@Test
	void toEntity() throws Exception {
		final var asset = AssetEntity.create().withId("assetId").withMunicipalityId(MUNICIPALITY_ID);
		final var file = new MockMultipartFile("attachment", "lokalritning.pdf", "application/pdf", CONTENT);

		final var result = toAssetAttachmentEntity(asset, file, file.getInputStream(), "LOKALRITNING", "description");

		assertThat(result).isNotNull();
		assertThat(result.getAsset()).isSameAs(asset);
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getFileName()).isEqualTo("lokalritning.pdf");
		assertThat(result.getMimeType()).isEqualTo("application/pdf");
		assertThat(result.getFileSize()).isEqualTo(CONTENT.length);
		assertThat(result.getCategory()).isEqualTo("LOKALRITNING");
		assertThat(result.getDescription()).isEqualTo("description");
		assertThat(result.getAttachmentData().getFile().getBinaryStream().readAllBytes()).isEqualTo(CONTENT);
	}

	@Test
	void toEntityCopiesTheWholeFile() throws Exception {
		final var asset = AssetEntity.create().withId("assetId");
		final var content = new byte[2048];
		content[2047] = 42;

		final var file = new MockMultipartFile("attachment", "big.pdf", "application/pdf", content);

		final var result = toAssetAttachmentEntity(asset, file, file.getInputStream(), null, null);

		assertThat(result.getFileSize()).isEqualTo(content.length);
		assertThat(result.getAttachmentData().getFile().getBinaryStream().readAllBytes()).isEqualTo(content);
	}

	@Test
	void copyData() throws Exception {
		final var source = AssetAttachmentDataEntity.create().withFile(new MariaDbBlob(CONTENT));

		final var result = copyAssetAttachmentData(source);

		assertThat(result).isNotNull().isNotSameAs(source);
		assertThat(result.getFile()).isNotSameAs(source.getFile());
		assertThat(result.getFile().getBinaryStream().readAllBytes()).isEqualTo(CONTENT);
	}

	@ParameterizedTest
	@MethodSource("copyDataWithoutContentArguments")
	void copyDataWithoutContent(final AssetAttachmentDataEntity input) {
		assertThat(copyAssetAttachmentData(input)).isNull();
	}

	private static Stream<AssetAttachmentDataEntity> copyDataWithoutContentArguments() {
		return Stream.of(null, AssetAttachmentDataEntity.create());
	}

	@Test
	void toAttachment() {
		final var created = OffsetDateTime.now();
		final var entity = AssetAttachmentEntity.create()
			.withId("id")
			.withFileName("lokalritning.pdf")
			.withMimeType("application/pdf")
			.withFileSize(1024)
			.withCategory("LOKALRITNING")
			.withDescription("description")
			.withCreated(created)
			.withAttachmentData(AssetAttachmentDataEntity.create().withFile(new MariaDbBlob(CONTENT)));

		final var result = toAssetAttachment(entity);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("id");
		assertThat(result.getFileName()).isEqualTo("lokalritning.pdf");
		assertThat(result.getMimeType()).isEqualTo("application/pdf");
		assertThat(result.getFileSize()).isEqualTo(1024);
		assertThat(result.getCategory()).isEqualTo("LOKALRITNING");
		assertThat(result.getDescription()).isEqualTo("description");
		assertThat(result.getCreated()).isEqualTo(created);
		assertThat(result.getUpdated()).isNull();
	}

	@Test
	void toAttachmentWithNullInput() {
		assertThat(toAssetAttachment(null)).isNull();
	}

	@Test
	void toAttachmentList() {
		final var entities = List.of(
			AssetAttachmentEntity.create().withId("1").withFileName("a.pdf"),
			AssetAttachmentEntity.create().withId("2").withFileName("b.pdf"));

		assertThat(toAssetAttachments(entities))
			.extracting(attachment -> attachment.getId(), attachment -> attachment.getFileName())
			.containsExactly(tuple("1", "a.pdf"), tuple("2", "b.pdf"));
	}

	@Test
	void toAttachmentListWithNullInput() {
		assertThat(toAssetAttachments(null)).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("updateEntityArguments")
	void updateEntityKeepsNullFields(final AssetAttachmentUpdateRequest request, final String expectedFileName, final String expectedCategory, final String expectedDescription) {
		final var entity = AssetAttachmentEntity.create().withFileName("old.pdf").withCategory("OLD").withDescription("old");

		final var result = updateEntity(entity, request);

		assertThat(result.getFileName()).isEqualTo(expectedFileName);
		assertThat(result.getCategory()).isEqualTo(expectedCategory);
		assertThat(result.getDescription()).isEqualTo(expectedDescription);
	}

	private static Stream<Arguments> updateEntityArguments() {
		return Stream.of(
			Arguments.of(null, "old.pdf", "OLD", "old"),
			Arguments.of(AssetAttachmentUpdateRequest.create(), "old.pdf", "OLD", "old"),
			Arguments.of(AssetAttachmentUpdateRequest.create().withCategory("NEW"), "old.pdf", "NEW", "old"),
			Arguments.of(
				AssetAttachmentUpdateRequest.create().withFileName("new.pdf").withCategory("NEW").withDescription("new"), "new.pdf", "NEW", "new"));
	}

	@Test
	void updateWithNullRequestReturnsTheSameEntity() {
		final var entity = AssetAttachmentEntity.create().withFileName("old.pdf");

		assertThat(updateEntity(entity, null)).isSameAs(entity);
	}
}
