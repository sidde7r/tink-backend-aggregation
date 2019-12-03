package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment.rpc;

import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import org.junit.Test;

public class InvestmentResponseTest {
    @Test
    public void testInvestmentResponse() {
        InvestmentResponse response =
                loadTestResponse("6.fondo_inversion_0097.xhtml", InvestmentResponse.class);

        assertEquals("BANKINTER EEUU NASDAQ 100 F", response.getName());
        assertEquals("ES0114105036", response.getIsin());
        assertEquals("EUR", response.getCurrency());
        assertEquals("01234567890123456", response.getFundAccount());
        assertEquals("ES2201281337857486299388", response.getAssociatedAccount());
        assertEquals(5789.35d, response.getTotalBalance().doubleValue(), 0.001);
        assertEquals(5789.35d, response.getAvailabeBalance().doubleValue(), 0.001);
        assertEquals(789.35d, response.getProfit().doubleValue(), 0.001);
        assertEquals(5000.0d, response.getContributions().doubleValue(), 0.001);
        assertEquals(2.64386d, response.getNumberOfShares().doubleValue(), 0.001);
        assertEquals(2196.91d, response.getSharePrice().doubleValue(), 0.001);
    }
}
