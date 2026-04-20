package se.sundsvall.partyassets.service.mapper;

import generated.se.sundsvall.relation.Relation;
import generated.se.sundsvall.relation.ResourceIdentifier;

public final class RelationMapper {

	private static final String TARGET_TYPE = "asset";
	private static final String TARGET_SERVICE = "partyassets";

	private RelationMapper() {}

	public static Relation toRelation(final String type, final se.sundsvall.dept44.support.Relation relation, final String assetId) {
		if (relation.getSource() == null) {
			return null;
		}
		return new Relation()
			.type(type)
			.source(new ResourceIdentifier()
				.resourceId(relation.getSource().getResourceId())
				.type(relation.getSource().getType())
				.service(relation.getSource().getService())
				.namespace(relation.getSource().getNamespace()))
			.target(new ResourceIdentifier()
				.resourceId(assetId)
				.type(TARGET_TYPE)
				.service(TARGET_SERVICE));
	}
}
