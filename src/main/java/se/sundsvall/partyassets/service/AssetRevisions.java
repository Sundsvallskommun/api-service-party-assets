package se.sundsvall.partyassets.service;

import java.util.Optional;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toRevision;

public final class AssetRevisions {

	private AssetRevisions() {}

	// A draft has not been published yet, so the way it was assembled is not history: the state it carries when it goes
	// active is revision 0, and only changes made after that are recorded. The actor is left alone for the same reason -
	// with no snapshot behind it, overwriting it would drop whoever assembled the draft and leave nothing to recover it
	// from.
	public static Optional<AssetRevisionEntity> snapshot(final AssetEntity asset) {
		if (asset.getStatus() == DRAFT) {
			return Optional.empty();
		}

		final var revision = toRevision(asset);
		asset.setActor(currentActor());
		asset.setRevision(asset.getRevision() + 1);

		return Optional.of(revision);
	}
}
