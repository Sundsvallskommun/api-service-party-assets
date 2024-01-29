package se.sundsvall.partyassets.pr3import;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void testGettersAndSetters() {
        var result = new PR3Importer.Result();
        result.setTotal(456);
        result.setFailed(123);
        result.setFailedExcelData(new byte[] { 1, 2, 3, 4, 5});

        assertThat(result.getTotal()).isEqualTo(456);
        assertThat(result.getFailed()).isEqualTo(123);
        assertThat(result.getSuccessful()).isEqualTo(result.getTotal() - result.getFailed());
        assertThat(result.getFailedExcelData()).hasSize(5);
    }
}
