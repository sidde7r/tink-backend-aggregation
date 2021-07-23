package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.CardAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CardAccountEntityTest {
    private static final String ACCOUNT_WITH_AVAILABLE_CREDIT =
            "{\n"
                    + "  \"resourceId\": \"123456789000000000000000000000000000\",\n"
                    + "  \"maskedPan\": \"525412******1234\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"name\": \"123456789\",\n"
                    + "  \"product\": \"SAS EuroBonus World Mastercard\",\n"
                    + "  \"balances\": [\n"
                    + "    {\n"
                    + "      \"balanceType\": \"interimAvaliable\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"currency\": \"SEK\",\n"
                    + "        \"amount\": 20000.73\n"
                    + "      },\n"
                    + "      \"creditLimitincluded\": true\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"balanceType\": \"expected\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"currency\": \"SEK\",\n"
                    + "        \"amount\": 29999.27\n"
                    + "      },\n"
                    + "      \"creditLimitincluded\": false\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"balanceType\": \"nonInvoiced\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"currency\": \"SEK\",\n"
                    + "        \"amount\": 1000.73\n"
                    + "      },\n"
                    + "      \"creditLimitincluded\": false\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"creditLimit\": {\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"amount\": 50000.0\n"
                    + "  },\n"
                    + "  \"status\": \"enabled\",\n"
                    + "  \"usage\": \"202\"\n"
                    + "}";

    private static final String ACCOUNT_WITHOUT_AVAILABLE_CREDIT =
            "{\n"
                    + "  \"resourceId\": \"123456789000000000000000000000000000\",\n"
                    + "  \"maskedPan\": \"525412******1234\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"name\": \"123456789\",\n"
                    + "  \"product\": \"SAS EuroBonus World Mastercard\",\n"
                    + "  \"balances\": [\n"
                    + "    {\n"
                    + "      \"balanceType\": \"expected\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"currency\": \"SEK\",\n"
                    + "        \"amount\": 29999.27\n"
                    + "      },\n"
                    + "      \"creditLimitincluded\": false\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"balanceType\": \"nonInvoiced\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"currency\": \"SEK\",\n"
                    + "        \"amount\": 1000.73\n"
                    + "      },\n"
                    + "      \"creditLimitincluded\": false\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"creditLimit\": {\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"amount\": 50000.0\n"
                    + "  },\n"
                    + "  \"status\": \"enabled\",\n"
                    + "  \"usage\": \"202\"\n"
                    + "}";

    @Test
    public void testParseBalances() {
        CardAccountEntity entity =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_WITH_AVAILABLE_CREDIT, CardAccountEntity.class);
        final CreditCardAccount account = entity.toTinkAccount();
        assertEquals(ExactCurrencyAmount.of("-29999.27", "SEK"), account.getExactBalance());
        assertEquals(ExactCurrencyAmount.of("20000.73", "SEK"), account.getExactAvailableCredit());
    }

    @Test
    public void testCalculateAvailableCredit() {
        CardAccountEntity entity =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_WITHOUT_AVAILABLE_CREDIT, CardAccountEntity.class);
        final CreditCardAccount account = entity.toTinkAccount();
        assertEquals(ExactCurrencyAmount.of("-29999.27", "SEK"), account.getExactBalance());
        assertEquals(ExactCurrencyAmount.of("20000.73", "SEK"), account.getExactAvailableCredit());
    }
}
