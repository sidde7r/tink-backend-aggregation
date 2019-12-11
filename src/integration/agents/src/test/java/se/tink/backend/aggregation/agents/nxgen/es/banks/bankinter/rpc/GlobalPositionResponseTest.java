package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import org.junit.Test;

public class GlobalPositionResponseTest {
    @Test
    public void testGlobalPositionResponse() {
        GlobalPositionResponse response =
                loadTestResponse("1.extracto_integral.xhtml", GlobalPositionResponse.class);

        assertEquals(1, response.getNumberOfAccounts());
        assertEquals(0, response.getAccountIds().get(0).intValue());

        assertEquals(2, response.getNumberOfInvestments());
        assertEquals(
                "/fondos/secure/fondo_inversion.xhtml?INDEX_CTA=1&COD_FONDO=0097",
                response.getInvestmentLinks().get(0));
        assertEquals(
                "/fondos/secure/fondo_inversion.xhtml?INDEX_CTA=2&COD_FONDO=0073",
                response.getInvestmentLinks().get(1));
    }
}
