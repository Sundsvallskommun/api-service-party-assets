package se.sundsvall.partyassets.integration.db.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.partyassets.integration.db.model.PartyType.ENTERPRISE;
import static se.sundsvall.partyassets.integration.db.model.PartyType.PRIVATE;

class PartyTypeTest {

	@Test
	void testEnum() {
		assertThat(PartyType.values()).containsExactlyInAnyOrder(ENTERPRISE, PRIVATE);
	}
}
