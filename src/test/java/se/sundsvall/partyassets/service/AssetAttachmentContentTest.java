package se.sundsvall.partyassets.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssetAttachmentContentTest {

	private static final String FILE_NAME = "lokalritning.pdf";
	private static final String MIME_TYPE = "application/pdf";

	@Test
	void equalsAndHashCodeConsidersContent() {
		final var content = new AssetAttachmentContent(FILE_NAME, MIME_TYPE, "content".getBytes());
		final var equalContent = new AssetAttachmentContent(FILE_NAME, MIME_TYPE, "content".getBytes());
		final var otherContent = new AssetAttachmentContent(FILE_NAME, MIME_TYPE, "other".getBytes());

		assertThat(content).isEqualTo(content).isEqualTo(equalContent).isNotEqualTo(otherContent).isNotEqualTo(null).isNotEqualTo(FILE_NAME).hasSameHashCodeAs(equalContent);
		assertThat(content.hashCode()).isNotEqualTo(otherContent.hashCode());
	}

	@Test
	void toStringShowsContentSize() {
		assertThat(new AssetAttachmentContent(FILE_NAME, MIME_TYPE, "content".getBytes()))
			.hasToString("AssetAttachmentContent[fileName=lokalritning.pdf, mimeType=application/pdf, content=7 bytes]");
		assertThat(new AssetAttachmentContent(FILE_NAME, MIME_TYPE, null))
			.hasToString("AssetAttachmentContent[fileName=lokalritning.pdf, mimeType=application/pdf, content=0 bytes]");
	}
}
