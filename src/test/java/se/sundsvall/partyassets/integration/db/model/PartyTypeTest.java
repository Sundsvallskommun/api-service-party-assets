package se.sundsvall.partyassets.integration.db.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.partyassets.integration.db.model.PartyType.ENTERPRISE;
import static se.sundsvall.partyassets.integration.db.model.PartyType.PRIVATE;

import org.junit.jupiter.api.Test;

class PartyTypeTest {

	@Test
	void testEnum() {
		assertThat(PartyType.values()).containsExactlyInAnyOrder(ENTERPRISE, PRIVATE);
	}
}
