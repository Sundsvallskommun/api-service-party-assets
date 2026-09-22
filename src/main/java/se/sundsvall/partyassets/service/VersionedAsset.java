package se.sundsvall.partyassets.service;

import se.sundsvall.partyassets.api.model.Asset;

/**
 * An asset together with the version its ETag is built from, so that the resource can offer the caller a precondition
 * to send back.
 */
public record VersionedAsset(Asset asset, Long version) {
}
