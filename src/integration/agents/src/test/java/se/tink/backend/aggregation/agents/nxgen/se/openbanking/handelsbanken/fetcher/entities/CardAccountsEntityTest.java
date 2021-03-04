package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity.CardAccountsEntity;

public class CardAccountsEntityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String cardAccountsEntity =
            "{\n"
                    + "  \"_links\": {\n"
                    + "    \"transactions\": {\n"
                    + "      \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v1/card-accounts/5ded9cd3-1234-1234-1234-26243af07f92/transactions\"\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"accountId\": \"5ded9cd3-1234-1234-1234-26243af07f92\",\n"
                    + "  \"balances\": [\n"
                    + "    {\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": 12345.67,\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      },\n"
                    + "      \"balanceType\": \"AVAILABLE_AMOUNT\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"cards\": [\n"
                    + "    {\n"
                    + "      \"maskedPan\": \"5226********1234\",\n"
                    + "      \"name\": \"Test Testsson\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"product\": \"Allkort Mastercard\"\n"
                    + "}";

    private String cardAccountsEntityEmptyCard =
            "{\n"
                    + "  \"_links\": {\n"
                    + "    \"transactions\": {\n"
                    + "      \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v1/card-accounts/5ded9cd3-1234-1234-1234-26243af07f92/transactions\"\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"accountId\": \"5ded9cd3-1234-1234-1234-26243af07f92\",\n"
                    + "  \"balances\": [\n"
                    + "    {\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": 12345.67,\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      },\n"
                    + "      \"balanceType\": \"AVAILABLE_AMOUNT\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"cards\": [],\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"product\": \"Allkort Mastercard\"\n"
                    + "}";

    @Test
    public void testCreditCardParsing() throws IOException {
        CardAccountsEntity cardAccounts =
                MAPPER.readValue(cardAccountsEntity, CardAccountsEntity.class);
        assertThat("Test Testsson").isEqualTo(cardAccounts.getHolderName());
    }

    @Test
    public void testCreditCardParsingNoCard() throws IOException {
        CardAccountsEntity cardAccounts =
                MAPPER.readValue(cardAccountsEntityEmptyCard, CardAccountsEntity.class);
        assertThat(cardAccounts.getHolderName()).isNullOrEmpty();
    }
}
