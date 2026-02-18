package se.sundsvall.partyassets.pr3import;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

	@Test
	void testGettersAndSetters() {
		final var result = new PR3Importer.Result()
			.withTotal(456)
			.withFailed(123)
			.withFailedExcelData(new byte[] {
				1, 2, 3, 4, 5
			});

		assertThat(result.getTotal()).isEqualTo(456);
		assertThat(result.getFailed()).isEqualTo(123);
		assertThat(result.getSuccessful()).isEqualTo(result.getTotal() - result.getFailed());
		assertThat(result.getFailedExcelData()).hasSize(5);
	}

}
