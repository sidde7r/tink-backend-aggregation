package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UrlUtilTest {

    @Test
    public void testFetchPaymentIdFromUrl() {
        assertThat(
                        UrlParseUtil.idFromUrl(
                                "https://psd.lcl.fr/pisp/payment-requests/d6ff3455-9e61-35e6-a86f-38e594a3705d"))
                .isEqualTo("d6ff3455-9e61-35e6-a86f-38e594a3705d");
    }
}
