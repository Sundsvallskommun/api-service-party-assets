package se.sundsvall.partyassets.service;

import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toRevision;

public final class AssetRevisions {

	private AssetRevisions() {}

	public static AssetRevisionEntity snapshot(final AssetEntity asset) {
		if (asset.getStatus() == DRAFT) {
			return null;
		}

		final var revision = toRevision(asset);
		asset.setActor(currentActor());
		asset.setRevision(asset.getRevision() + 1);

		return revision;
	}
}
