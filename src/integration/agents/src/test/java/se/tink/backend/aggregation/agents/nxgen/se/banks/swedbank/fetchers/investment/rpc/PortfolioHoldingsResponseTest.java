package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PortfolioHoldingsResponseTest {

    @Test
    public void shouldReturnEmptyListIfNoInvestmentAccountsExist() {
        PortfolioHoldingsResponse portfolioHoldingsResponse =
                SerializationUtils.deserializeFromString(
                        getEmptyPortfolio(), PortfolioHoldingsResponse.class);

        List<String> result = portfolioHoldingsResponse.getInvestmentAccountNumbers();

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnAccountNumberListIfInvestmentAccountsExist() {
        PortfolioHoldingsResponse portfolioHoldingsResponse =
                SerializationUtils.deserializeFromString(
                        getPortfolio(), PortfolioHoldingsResponse.class);

        List<String> result = portfolioHoldingsResponse.getInvestmentAccountNumbers();

        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("8 123 456-9", result.get(0));
        Assert.assertEquals("8105-9,333 333 555-6", result.get(1));
    }

    private String getPortfolio() {
        return "{"
                + "  \"fundAccounts\": ["
                + "    {"
                + "      \"totalValue\": {"
                + "        \"amount\": \"1,03\","
                + "        \"currencyCode\": \"SEK\""
                + "      },"
                + "      \"type\": \"FUNDACCOUNT\","
                + "      \"rightOfDisposal\": false,"
                + "      \"performance\": {"
                + "        \"percent\": \"3,00\","
                + "        \"amount\": {"
                + "          \"amount\": \"0,03\","
                + "          \"currencyCode\": \"SEK\""
                + "        }"
                + "      },"
                + "      \"marketValue\": {"
                + "        \"amount\": \"1,03\","
                + "        \"currencyCode\": \"SEK\""
                + "      },"
                + "      \"holdings\": ["
                + "        {"
                + "          \"name\": \"This is a fundAccount\","
                + "          \"fundCode\": \"RKP\""
                + "        }"
                + "      ],"
                + "      \"id\": \"ffffffffffffffffffffffffffffffffff123123\","
                + "      \"name\": \"Fond\","
                + "      \"accountNumber\": \"8 123 456-9\","
                + "      \"clearingNumber\": \"08999\","
                + "      \"fullyFormattedNumber\": \"8 123 456-9\""
                + "    }"
                + "  ],"
                + "  \"endowmentInsurances\": [],"
                + "  \"equityTraders\": [],"
                + "  \"savingsAccounts\": [],"
                + "  \"investmentSavings\": ["
                + "    {"
                + "      \"totalValue\": {"
                + "        \"amount\": \"595 777,37\","
                + "        \"currencyCode\": \"SEK\""
                + "      },"
                + "      \"type\": \"ISK\","
                + "      \"rightOfDisposal\": false,"
                + "      \"performance\": {"
                + "        \"percent\": \"52,38\","
                + "        \"amount\": {"
                + "          \"amount\": \"204 792,50\","
                + "          \"currencyCode\": \"SEK\""
                + "        }"
                + "      },"
                + "      \"marketValue\": {"
                + "        \"amount\": \"595 777,23\","
                + "        \"currencyCode\": \"SEK\""
                + "      },"
                + "      \"holdings\": ["
                + "        {"
                + "          \"name\": \"This is my Investment\","
                + "          \"fundCode\": \"XPD\""
                + "        }"
                + "      ],"
                + "      \"id\": \"dddddddddddddddddddddddddddddddddddddddd\","
                + "      \"name\": \"Investeringssparande\","
                + "      \"accountNumber\": \"333 333 555-6\","
                + "      \"clearingNumber\": \"8105-9\","
                + "      \"fullyFormattedNumber\": \"8105-9,333 333 555-6\""
                + "    }"
                + "  ]"
                + "}";
    }

    private String getEmptyPortfolio() {
        return "{ \"fundAccounts\": [],"
                + "    \"endowmentInsurances\": [],"
                + "    \"equityTraders\": [],"
                + "    \"savingsAccounts\": [],"
                + "    \"investmentSavings\": []}";
    }
}
