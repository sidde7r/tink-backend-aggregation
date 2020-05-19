package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FilterAccountsResponseTest {

    @Test
    public void getTinkAccounts() {
        // given
        FilterAccountsResponse response = getTestData();

        // when
        Collection<TransactionalAccount> accounts =
                response.getTinkAccounts(accountNumber -> accountNumber);

        // then
        assertThat(accounts).isNotNull();
        assertThat(accounts).hasSize(1);
        for (TransactionalAccount account : accounts) {
            assertThat(account.getName()).isNotNull();
            assertThat(account.getApiIdentifier()).isNotNull();
            assertThat(account.getAccountNumber()).isNotNull();
            assertThat(account.getExactBalance().getDoubleValue()).isNotEqualTo(0);
        }
    }

    private static FilterAccountsResponse getTestData() {
        return SerializationUtils.deserializeFromString(TEST_DATA, FilterAccountsResponse.class);
    }

    private static final String TEST_DATA =
            "[{"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9970.5251088836\","
                    + "\"agreementId\": \"111000696169744\""
                    + "},"
                    + "\"localizedAccountId\": \"525.101.363-6\","
                    + "\"id\": \"9970.5251088836\","
                    + "\"type\": \"KS-KONTO\","
                    + "\"currency\": \"SEK\","
                    + "\"name\": \"Privatkonto\","
                    + "\"sortNumber\": 1,"
                    + "\"amount\": {"
                    + "\"localizedValue\": \"233,54\","
                    + "\"localizedValueWithCurrency\": \"233,54 SEK\","
                    + "\"value\": 23354,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"233,54 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"234 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"234 SEK\""
                    + "},"
                    + "\"availableAmount\": {"
                    + "\"localizedValue\": \"233,54\","
                    + "\"localizedValueWithCurrency\": \"233,54 SEK\","
                    + "\"value\": 23354,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"233,54 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"234 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"234 SEK\""
                    + "},"
                    + "\"productElementType\": \"CISP\","
                    + "\"accountProperties\": {"
                    + "\"canRename\": false,"
                    + "\"showAvailableAmount\": false,"
                    + "\"debitable\": true,"
                    + "\"creditable\": true,"
                    + "\"mayQuery\": true,"
                    + "\"loan\": false,"
                    + "\"favorite\": true,"
                    + "\"likvidityZone\": true"
                    + "}"
                    + "},"
                    + "{"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"1170.5251013123\","
                    + "\"agreementId\": \"447000696169123\""
                    + "},"
                    + "\"localizedAccountId\": \"525.101.363-6\","
                    + "\"id\": \"1170.5251013123\","
                    + "\"type\": \"KS-KONTO\","
                    + "\"currency\": \"SEK\","
                    + "\"name\": \"Privatkonto\","
                    + "\"sortNumber\": 1,"
                    + "\"amount\": {"
                    + "\"localizedValue\": \"233,54\","
                    + "\"localizedValueWithCurrency\": \"233,54 SEK\","
                    + "\"value\": 23354,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"233,54 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"234 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"234 SEK\""
                    + "},"
                    + "\"availableAmount\": {"
                    + "\"localizedValue\": \"233,54\","
                    + "\"localizedValueWithCurrency\": \"233,54 SEK\","
                    + "\"value\": 23354,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"233,54 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"234 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"234 SEK\""
                    + "},"
                    + "\"productElementType\": \"CISP\","
                    + "\"accountProperties\": {"
                    + "\"canRename\": false,"
                    + "\"showAvailableAmount\": false,"
                    + "\"debitable\": true,"
                    + "\"creditable\": true,"
                    + "\"mayQuery\": true,"
                    + "\"loan\": true,"
                    + "\"favorite\": true,"
                    + "\"likvidityZone\": true"
                    + "}"
                    + "}]";
}
