package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PensionPortfoliosResponseTest {

    @Test
    public void shouldReturnTrueIfThereArePensionHoldings() {
        PensionPortfoliosResponse pensionPortfoliosResponse =
                SerializationUtils.deserializeFromString(
                        getPensionPortfolio(), PensionPortfoliosResponse.class);

        boolean result = pensionPortfoliosResponse.hasPensionHoldings();

        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfThereAreNoPensionHoldings() {
        PensionPortfoliosResponse pensionPortfoliosResponse =
                SerializationUtils.deserializeFromString(
                        getEmptyPortfolio(), PensionPortfoliosResponse.class);

        boolean result = pensionPortfoliosResponse.hasPensionHoldings();

        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnListOfAccountNumbersIfThereArePensionHoldings() {
        PensionPortfoliosResponse pensionPortfoliosResponse =
                SerializationUtils.deserializeFromString(
                        getPensionPortfolio(), PensionPortfoliosResponse.class);

        List<String> result = pensionPortfoliosResponse.getPensionAccountNumbers();

        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("8103-4,333 333 333-8", result.get(0));
        Assert.assertEquals("8104-4,333 333 444-4", result.get(1));
    }

    @Test
    public void shouldReturnEmptyListIfThereAreNoPensionHoldings() {
        PensionPortfoliosResponse pensionPortfoliosResponse =
                SerializationUtils.deserializeFromString(
                        getEmptyPortfolio(), PensionPortfoliosResponse.class);

        List<String> result = pensionPortfoliosResponse.getPensionAccountNumbers();

        Assert.assertTrue(result.isEmpty());
    }

    private String getEmptyPortfolio() {
        return "{ \"privatePensionInsurances\": {"
                + "    \"pensionInsurances\": []},"
                + "    \"occupationalPensionInsurances\":{"
                + "    \"pensionInsurances\": []}"
                + "    }";
    }

    private String getPensionPortfolio() {
        return "{"
                + "    \"privatePensionInsurances\": {"
                + "        \"pensionInsurances\": ["
                + "            {"
                + "                \"totalValue\": {"
                + "                    \"amount\": \"66 666,66\","
                + "                    \"currencyCode\": \"SEK\""
                + "                },"
                + "                \"type\": \"INDIVIDUAL_SAVINGS_PENSION\","
                + "                \"performance\": {"
                + "                    \"percent\": \"0,55\","
                + "                    \"amount\": {"
                + "                        \"amount\": \"0,00\","
                + "                        \"currencyCode\": \"SEK\""
                + "                    }"
                + "                },"
                + "                \"marketValue\": {"
                + "                    \"amount\": \"0,00\","
                + "                    \"currencyCode\": \"SEK\""
                + "                },"
                + "                \"holdingsFetched\": true,"
                + "                \"holdings\": ["
                + "                            {"
                + "                    \"name\": \"Swedbank Robur Ränta Kort Plus\","
                + "                    \"fundCode\": \"RKP\""
                + "                }  "
                + "                ],"
                + "                \"showAdvice\": false,"
                + "                \"payoutPeriodYears\": 0,"
                + "                \"id\": \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccc\","
                + "                \"name\": \"Individuellt pensionssparande\","
                + "                \"accountNumber\": \"333 333 333-8\","
                + "                \"clearingNumber\": \"8103-3\","
                + "                \"fullyFormattedNumber\": \"8103-4,333 333 333-8\""
                + "            }"
                + "        ],"
                + "        \"totalValue\": {"
                + "            \"amount\": \"66 666,66\","
                + "            \"currencyCode\": \"SEK\""
                + "        }"
                + "    },"
                + "    \"occupationalPensionInsurances\": {"
                + "        \"pensionInsurances\": ["
                + "            {"
                + "                \"totalValue\": {"
                + "                    \"amount\": \"666 666,66\","
                + "                    \"currencyCode\": \"SEK\""
                + "                },"
                + "                \"type\": \"OCCUPATIONAL_PENSION\","
                + "                \"performance\": {"
                + "                    \"percent\": \"33,33\","
                + "                    \"amount\": {"
                + "                        \"amount\": \"166 666,16\","
                + "                        \"currencyCode\": \"SEK\""
                + "                    }"
                + "                },"
                + "                \"marketValue\": {"
                + "                    \"amount\": \"0,00\","
                + "                    \"currencyCode\": \"SEK\""
                + "                },"
                + "                \"holdingsFetched\": true,"
                + "                \"holdings\": ["
                + "                            {"
                + "                    \"name\": \"Swedbank Robur Ränta Kort Plus\","
                + "                    \"fundCode\": \"RKP\""
                + "                }  "
                + "                ],"
                + "                \"isTrad\": false,"
                + "                \"showAdvice\": false,"
                + "                \"payoutAge\": 65,"
                + "                \"payoutPeriodYears\": 5,"
                + "                \"productCode\": \"IHP1FOND\","
                + "                \"id\": \"cccccccccccccccccccccccccccccccccccccccc\","
                + "                \"name\": \"ITP 1 pension fond\","
                + "                \"accountNumber\": \"333 333 444-4\","
                + "                \"clearingNumber\": \"8104-4\","
                + "                \"fullyFormattedNumber\": \"8104-4,333 333 444-4\""
                + "            }"
                + "        ],"
                + "        \"totalValue\": {"
                + "            \"amount\": \"666 666,66\","
                + "            \"currencyCode\": \"SEK\""
                + "        }"
                + "    },"
                + "    \"serverTime\": \"00:29\","
                + "    \"totalValue\": {"
                + "        \"amount\": \"0,00\","
                + "        \"currencyCode\": \"SEK\""
                + "    }"
                + "}";
    }
}
