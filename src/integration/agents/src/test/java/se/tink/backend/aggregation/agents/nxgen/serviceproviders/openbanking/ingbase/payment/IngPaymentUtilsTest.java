package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IngPaymentUtilsTest {

    @Test
    public void notModifyIfMarketOtherThanFrance() {
        // do not modify market code if market isn't France
        String authorizationUrl = "http://something.com/XX";
        assertEquals(authorizationUrl, IngPaymentUtils.modifyMarketCode(authorizationUrl, "DE"));
    }

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
    public void modifyIfMarketFrance() {
        assertEquals(
                "http://something.com/FR",
                IngPaymentUtils.modifyMarketCode("http://something.com/XX", "FR"));
    }
}
