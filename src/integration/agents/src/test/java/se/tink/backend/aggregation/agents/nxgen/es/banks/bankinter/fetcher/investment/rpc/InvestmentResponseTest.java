package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment.rpc;

import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

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

    @Test
    public void testParseInvestmentTransactions() {
        InvestmentResponse response =
                loadTestResponse("6.fondo_inversion_0097.xhtml", InvestmentResponse.class);
        List<Transaction> transactions = response.toTinkTransactions();

        assertEquals(1, transactions.size());

        Transaction t = transactions.iterator().next();
        assertEquals("PRIMERA SUSCRIP.", t.getDescription());
        assertEquals(BigDecimal.valueOf(1234.56), t.getExactAmount().getExactValue());
        assertEquals("2019-01-25", formatDate(t.getDate()));
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
