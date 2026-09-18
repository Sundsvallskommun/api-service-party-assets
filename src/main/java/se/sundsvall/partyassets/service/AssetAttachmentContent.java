package se.sundsvall.partyassets.service;

import java.util.Arrays;
import java.util.Objects;

/**
 * An attachment's file, read out of the database so that the response can be written without holding a connection.
 */
public record AssetAttachmentContent(String fileName, String mimeType, byte[] content) {

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final AssetAttachmentContent other)) {
			return false;
		}
		return Objects.equals(fileName, other.fileName) && Objects.equals(mimeType, other.mimeType) && Arrays.equals(content, other.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileName, mimeType, Arrays.hashCode(content));
	}

	@Override
	public String toString() {
		return "AssetAttachmentContent[fileName=%s, mimeType=%s, content=%d bytes]".formatted(fileName, mimeType, content == null ? 0 : content.length);
	}
}
