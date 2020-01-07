package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import org.junit.Test;

public class GlobalPositionResponseTest {
    @Test
    public void testGlobalPositionResponse() {
        GlobalPositionResponse response =
                loadTestResponse("1.extracto_integral.xhtml", GlobalPositionResponse.class);

        assertEquals(2, response.getAccountLinks().size());
        assertEquals(
                "/extracto/secure/movimientos_cuenta.xhtml?INDEX_CTA=0&IND=N",
                response.getAccountLinks().get(0));
        assertEquals(
                "/extracto/secure/movimientos_cuenta.xhtml?INDEX_CTA=6&IND=C",
                response.getAccountLinks().get(1));

        assertEquals(2, response.getInvestmentLinks().size());
        assertEquals(
                "/fondos/secure/fondo_inversion.xhtml?INDEX_CTA=1&COD_FONDO=0097",
                response.getInvestmentLinks().get(0));
        assertEquals(
                "/fondos/secure/fondo_inversion.xhtml?INDEX_CTA=2&COD_FONDO=0073",
                response.getInvestmentLinks().get(1));

        assertEquals(1, response.getCreditCardLinks().size());
        assertEquals(
                "/tarjetas/secure/tarjetas_ficha.xhtml?INDEX_CTA=3",
                response.getCreditCardLinks().get(0));
    }
}
