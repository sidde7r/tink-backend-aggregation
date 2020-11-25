package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.creditcard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetCreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetCreditCardsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class OpBankCreditCardAccountFetcherTestFixtures {

    static GetCreditCardsResponse creditCardsResponse() {
        return SerializationUtils.deserializeFromString(
                "[\n"
                        + "  {\n"
                        + "    \"cardId\": \"testCardId1\",\n"
                        + "    \"cardNumber\": \"1234 56** **** 7890\",\n"
                        + "    \"productName\": \"testProductName1\"\n"
                        + "  },\n"
                        + "  {\n"
                        + "    \"cardId\": \"testCardId2\",\n"
                        + "    \"cardNumber\": \"0987 65** **** 4321\",\n"
                        + "    \"productName\": \"testProductName2\"\n"
                        + "  }\n"
                        + "]",
                GetCreditCardsResponse.class);
    }

    static GetCreditCardTransactionsResponse creditCardTransactionsPage1() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "\n"
                        + "    \"next\": {\n"
                        + "    \t\"href\":\"/nextKey\"\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"transactions\": [\n"
                        + "    {\n"
                        + "      \"amount\": \"3.50\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"description\": \"testDescription\",\n"
                        + "      \"postingDate\": \"2020-11-09\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                GetCreditCardTransactionsResponse.class);
    }

    static GetCreditCardTransactionsResponse creditCardTransactionsPage2() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "  },\n"
                        + "  \"transactions\": [\n"
                        + "    {\n"
                        + "      \"amount\": \"-3.50\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"description\": \"testDescription2\",\n"
                        + "      \"postingDate\": \"2020-12-09\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n"
                        + "\n",
                GetCreditCardTransactionsResponse.class);
    }
}
