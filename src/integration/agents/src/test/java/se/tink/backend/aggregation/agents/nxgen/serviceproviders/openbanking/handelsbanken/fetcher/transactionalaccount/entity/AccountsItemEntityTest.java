package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountsItemEntityTest {

    private AccountsItemEntity accountsItemEntity;

    @Before
    public void setup() {
        this.accountsItemEntity = getAccountsItemEntity();
    }

    @Test
    public void shouldMapToTinkAccount() {
        // given
        AccountDetailsResponse accountDetailsResponse = getAccountDetailsResponse();

        // when
        Optional<TransactionalAccount> optionalTransactionalAccount =
                accountsItemEntity.toTinkAccount(
                        TransactionalAccountType.CHECKING, accountDetailsResponse);

        // then
        assertTrue(optionalTransactionalAccount.isPresent());
        TransactionalAccount transactionalAccount = optionalTransactionalAccount.get();
        assertThat(transactionalAccount.getIdModule().getUniqueId()).isEqualTo("123456789");
        assertThat(transactionalAccount.getIdModule().getAccountNumber())
                .isEqualTo("6602-123456789");
        assertThat(transactionalAccount.getIdModule().getAccountName()).isEqualTo("Privatkonto");
        assertThat(transactionalAccount.getApiIdentifier())
                .isEqualTo("d8d046e7-9bef-4b02-a8ad-816eb09f3968");
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getExactBalance().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(3000.64));
        assertThat(transactionalAccount.getExactBalance().getCurrencyCode()).isEqualTo("SEK");
        assertThat(transactionalAccount.getIdentifiers().size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnOptionalEmptyIfAvailableBalanceIsNotPresent() {
        // given
        AccountDetailsResponse accountDetailsResponse =
                getAccountDetailsResponseWithoutAvailableBalance();

        // when
        Optional<TransactionalAccount> optionalTransactionalAccount =
                accountsItemEntity.toTinkAccount(
                        TransactionalAccountType.CHECKING, accountDetailsResponse);

        // then
        assertThat(optionalTransactionalAccount).isNotPresent();
    }

    private AccountsItemEntity getAccountsItemEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"transactions\": {\n"
                        + "      \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/d8d046e7-9bef-4b02-a8ad-816eb09f3968/transactions\"\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"accountId\": \"d8d046e7-9bef-4b02-a8ad-816eb09f3968\",\n"
                        + "  \"accountType\": \"Privatkonto\",\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"amount\": {\n"
                        + "        \"content\": 3000.64,\n"
                        + "        \"currency\": \"SEK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"AVAILABLE_AMOUNT\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"amount\": {\n"
                        + "        \"content\": 3000.64,\n"
                        + "        \"currency\": \"SEK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"CURRENT\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"bban\": \"123456789\",\n"
                        + "  \"bic\": \"HANDSESS\",\n"
                        + "  \"clearingNumber\": \"6602\",\n"
                        + "  \"currency\": \"SEK\",\n"
                        + "  \"iban\": \"SE7160000000000123456789\",\n"
                        + "  \"ownerName\": \"FirstName LastName\"\n"
                        + "}",
                AccountsItemEntity.class);
    }

    private AccountDetailsResponse getAccountDetailsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"transactions\": {\n"
                        + "      \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/d8d046e7-9bef-4b02-a8ad-816eb09f3968/transactions\"\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"accountId\": \"d8d046e7-9bef-4b02-a8ad-816eb09f3968\",\n"
                        + "  \"accountType\": \"Privatkonto\",\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"amount\": {\n"
                        + "        \"content\": 3000.64,\n"
                        + "        \"currency\": \"SEK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"AVAILABLE_AMOUNT\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"amount\": {\n"
                        + "        \"content\": 3000.64,\n"
                        + "        \"currency\": \"SEK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"CURRENT\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"bban\": \"123456789\",\n"
                        + "  \"bic\": \"HANDSESS\",\n"
                        + "  \"clearingNumber\": \"6602\",\n"
                        + "  \"currency\": \"SEK\",\n"
                        + "  \"iban\": \"SE7160000000000123456789\",\n"
                        + "  \"ownerName\": \"FirstName LastName\"\n"
                        + "}",
                AccountDetailsResponse.class);
    }

    private AccountDetailsResponse getAccountDetailsResponseWithoutAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"transactions\": {\n"
                        + "      \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/d8d046e7-9bef-4b02-a8ad-816eb09f3968/transactions\"\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"accountId\": \"d8d046e7-9bef-4b02-a8ad-816eb09f3968\",\n"
                        + "  \"accountType\": \"Privatkonto\",\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"amount\": {\n"
                        + "        \"content\": 3000.64,\n"
                        + "        \"currency\": \"SEK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"CURRENT\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"bban\": \"123456789\",\n"
                        + "  \"bic\": \"HANDSESS\",\n"
                        + "  \"clearingNumber\": \"6602\",\n"
                        + "  \"currency\": \"SEK\",\n"
                        + "  \"iban\": \"SE7160000000000123456789\",\n"
                        + "  \"ownerName\": \"FirstName LastName\"\n"
                        + "}",
                AccountDetailsResponse.class);
    }
}
