package se.sundsvall.partyassets.service;

import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toRevision;

public final class AssetRevisions {

	private AssetRevisions() {}

	/**
	 * Records the current actor on the asset, moves it to its next revision and returns its previous state for the history.
	 * A draft gets the actor only, no revision, and returns null.
	 */
	public static AssetRevisionEntity advanceRevision(final AssetEntity asset) {
		final var revision = asset.getStatus() == DRAFT ? null : toRevision(asset);
		asset.setActor(currentActor());
		if (revision != null) {
			asset.setRevision(asset.getRevision() + 1);
		}

		return revision;
	}
}
