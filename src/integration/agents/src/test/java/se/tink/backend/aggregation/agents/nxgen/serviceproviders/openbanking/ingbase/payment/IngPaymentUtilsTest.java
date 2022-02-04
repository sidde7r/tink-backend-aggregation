package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IngPaymentUtilsTest {

    @Test
    public void notModifyIfAuthorizationUrlNull() {
        String authorizationUrl = null;
        assertEquals(authorizationUrl, IngPaymentUtils.modifyMarketCode(authorizationUrl, "FR"));
    }

    @Test
    public void notModifyIfAuthorizationUrlShorterThanTwoCharacters() {
        String authorizationUrl = "a";
        assertEquals(authorizationUrl, IngPaymentUtils.modifyMarketCode(authorizationUrl, "FR"));
    }

    @Test
    public void modifyMarketIfNeeded() {
        assertEquals(
                "http://something.com/FR",
                IngPaymentUtils.modifyMarketCode("http://something.com/XX", "FR"));
    }
}
