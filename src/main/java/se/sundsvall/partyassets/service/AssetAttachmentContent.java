package se.sundsvall.partyassets.service;

/**
 * An attachment's file, read out of the database so that the response can be written without holding a connection.
 */
public record AssetAttachmentContent(String fileName, String mimeType, byte[] content) {}
