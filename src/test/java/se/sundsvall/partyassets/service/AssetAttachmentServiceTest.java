package se.sundsvall.partyassets.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mariadb.jdbc.MariaDbBlob;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetAttachmentUpdateRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetAttachmentRepository;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentDataEntity;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class AssetAttachmentServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ASSET_ID = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
	private static final String ATTACHMENT_ID = "5f79a808-0ef3-4985-99b9-b12f23e202a7";
	private static final String FILE_NAME = "lokalritning.pdf";
	private static final String MIME_TYPE = "application/pdf";
	private static final byte[] CONTENT = "content".getBytes();

	@Mock
	private AssetRepository assetRepositoryMock;

	@Mock
	private AssetAttachmentRepository attachmentRepositoryMock;

	@InjectMocks
	private AssetAttachmentService service;

	@Captor
	private ArgumentCaptor<AssetAttachmentEntity> attachmentCaptor;

	private static AssetEntity asset(final Status status) {
		return AssetEntity.create().withId(ASSET_ID).withMunicipalityId(MUNICIPALITY_ID).withStatus(status);
	}

	private static AssetAttachmentEntity attachment(final Status assetStatus) {
		return AssetAttachmentEntity.create()
			.withId(ATTACHMENT_ID)
			.withAsset(asset(assetStatus))
			.withFileName(FILE_NAME)
			.withMimeType(MIME_TYPE)
			.withAttachmentData(AssetAttachmentDataEntity.create().withFile(new MariaDbBlob(CONTENT)));
	}

	private static MockMultipartFile file() {
		return new MockMultipartFile("attachment", FILE_NAME, MIME_TYPE, CONTENT);
	}

	@Test
	void createAttachment() {
		when(assetRepositoryMock.findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(asset(Status.ACTIVE)));
		when(attachmentRepositoryMock.saveAndFlush(any(AssetAttachmentEntity.class))).thenReturn(attachment(Status.ACTIVE));

		final var result = service.createAttachment(MUNICIPALITY_ID, ASSET_ID, file(), "LOKALRITNING", "description");

		assertThat(result).isEqualTo(ATTACHMENT_ID);
		verify(assetRepositoryMock).findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).saveAndFlush(attachmentCaptor.capture());
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);

		assertThat(attachmentCaptor.getValue()).satisfies(saved -> {
			assertThat(saved.getAsset().getId()).isEqualTo(ASSET_ID);
			assertThat(saved.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(saved.getFileName()).isEqualTo(FILE_NAME);
			assertThat(saved.getMimeType()).isEqualTo(MIME_TYPE);
			assertThat(saved.getFileSize()).isEqualTo(CONTENT.length);
			assertThat(saved.getCategory()).isEqualTo("LOKALRITNING");
			assertThat(saved.getDescription()).isEqualTo("description");
			assertThat(saved.getAttachmentData().getFile().getBinaryStream().readAllBytes()).isEqualTo(CONTENT);
		});
	}

	@Test
	void createAttachmentOnNonExistingAsset() {
		when(assetRepositoryMock.findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAttachment(MUNICIPALITY_ID, ASSET_ID, file(), null, null))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));

		verify(assetRepositoryMock).findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock, never()).saveAndFlush(any());
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void createAttachmentOnExpiredAsset() {
		when(assetRepositoryMock.findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(asset(Status.EXPIRED)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAttachment(MUNICIPALITY_ID, ASSET_ID, file(), null, null))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST));

		verify(assetRepositoryMock).findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock, never()).saveAndFlush(any());
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void createAttachmentWithTooLongFileName() {
		final var fileName = "a".repeat(256) + ".pdf";
		when(assetRepositoryMock.findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(asset(Status.ACTIVE)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAttachment(MUNICIPALITY_ID, ASSET_ID, new MockMultipartFile("attachment", fileName, MIME_TYPE, CONTENT), null, null))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST));

		verify(attachmentRepositoryMock, never()).saveAndFlush(any());
	}

	@Test
	void createAttachmentWithEmptyFile() {
		when(assetRepositoryMock.findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(asset(Status.ACTIVE)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAttachment(MUNICIPALITY_ID, ASSET_ID, new MockMultipartFile("attachment", FILE_NAME, MIME_TYPE, new byte[0]), null, null))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST));

		verify(attachmentRepositoryMock, never()).saveAndFlush(any());
	}

	@Test
	void createAttachmentWhenTheUploadCannotBeRead() throws IOException {
		final var file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getOriginalFilename()).thenReturn(FILE_NAME);
		when(file.getInputStream()).thenThrow(new IOException("boom"));
		when(assetRepositoryMock.findByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(asset(Status.ACTIVE)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAttachment(MUNICIPALITY_ID, ASSET_ID, file, null, null))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR));

		verify(attachmentRepositoryMock, never()).saveAndFlush(any());
	}

	@Test
	void readAttachments() {
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findAllForAsset(ASSET_ID, MUNICIPALITY_ID)).thenReturn(List.of(attachment(Status.ACTIVE)));

		final var result = service.readAttachments(MUNICIPALITY_ID, ASSET_ID);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getFileName()).isEqualTo(FILE_NAME);
		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findAllForAsset(ASSET_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void readAttachmentsOnNonExistingAsset() {
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(false);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.readAttachments(MUNICIPALITY_ID, ASSET_ID))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));

		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(assetRepositoryMock);
		verifyNoInteractions(attachmentRepositoryMock);
	}

	@Test
	void readAttachment() {
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(attachment(Status.EXPIRED)));

		final var result = service.readAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID);

		assertThat(result.fileName()).isEqualTo(FILE_NAME);
		assertThat(result.mimeType()).isEqualTo(MIME_TYPE);
		assertThat(result.content()).isEqualTo(CONTENT);
		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void readNonExistingAttachment() {
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.readAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));

		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void updateAttachment() {
		final var entity = attachment(Status.DRAFT);
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(attachmentRepositoryMock.saveAndFlush(entity)).thenReturn(entity);

		final var result = service.updateAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID, AssetAttachmentUpdateRequest.create().withCategory("LOKALRITNING"));

		assertThat(result.getCategory()).isEqualTo("LOKALRITNING");
		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).saveAndFlush(entity);
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void updateAttachmentOnReplacedAsset() {
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(attachment(Status.REPLACED)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.updateAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID, AssetAttachmentUpdateRequest.create()))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST));

		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock, never()).saveAndFlush(any());
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void deleteAttachment() {
		final var entity = attachment(Status.ACTIVE);
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		service.deleteAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID);

		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).delete(entity);
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void deleteAttachmentOnExpiredAsset() {
		when(assetRepositoryMock.existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(attachment(Status.EXPIRED)));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.deleteAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST));

		verify(assetRepositoryMock).existsByIdAndMunicipalityId(ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock).findByIdForAsset(ATTACHMENT_ID, ASSET_ID, MUNICIPALITY_ID);
		verify(attachmentRepositoryMock, never()).delete(any());
		verifyNoMoreInteractions(assetRepositoryMock, attachmentRepositoryMock);
	}
}
