package se.sundsvall.partyassets.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.BLOCKED;
import static se.sundsvall.partyassets.api.model.Status.EXPIRED;
import static se.sundsvall.partyassets.api.model.Status.TEMPORARY;

import org.junit.jupiter.api.Test;

class StatusTest {

	@Test
	void testEnumValues() {
		assertThat(Status.values()).containsExactlyInAnyOrder(ACTIVE, EXPIRED, BLOCKED, TEMPORARY);
	}
}
