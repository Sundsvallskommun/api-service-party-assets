package se.sundsvall.partyassets.service.mapper;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.support.Relation;
import se.sundsvall.dept44.support.Relation.ResourceIdentifier;

import static org.assertj.core.api.Assertions.assertThat;

class RelationMapperTest {

	@Test
	void toRelation() {
		// Arrange
		final var type = "LINK";
		final var assetId = "asset-123";
		final var sourceId = "source-id";
		final var sourceType = "case";
		final var sourceService = "caseservice";
		final var sourceNamespace = "MY_NAMESPACE";
		final var relation = Relation.create(type,
			ResourceIdentifier.create(sourceId, sourceType, sourceService, sourceNamespace),
			ResourceIdentifier.create("target-id", "asset", "partyassets", null));

		// Act
		final var result = RelationMapper.toRelation(type, relation, assetId);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getSource().getResourceId()).isEqualTo(sourceId);
		assertThat(result.getSource().getType()).isEqualTo(sourceType);
		assertThat(result.getSource().getService()).isEqualTo(sourceService);
		assertThat(result.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(result.getTarget().getResourceId()).isEqualTo(assetId);
		assertThat(result.getTarget().getType()).isEqualTo("asset");
		assertThat(result.getTarget().getService()).isEqualTo("partyassets");
	}

	@Test
	void toRelationWithNullSource() {
		// Arrange
		final var relation = new Relation().withType("LINK");

		// Act
		final var result = RelationMapper.toRelation("LINK", relation, "asset-123");

		// Assert
		assertThat(result).isNull();
	}
}
