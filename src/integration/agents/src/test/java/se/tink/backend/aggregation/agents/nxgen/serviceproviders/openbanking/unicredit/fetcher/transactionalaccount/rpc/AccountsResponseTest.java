package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account.AccountEntity;

public class AccountsResponseTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void getAccountsWithEmptyAccountsInResponse() {
        // given
        AccountsResponse accountsResponse = accountsAsResponse(Collections.emptyList());

        // when
        List<AccountEntity> result = accountsResponse.getAccounts();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAccountsWithExistingAccountsInResponse() {
        // given
        AccountsResponse accountsResponse =
                accountsAsResponse(Arrays.asList(accountEntityProps("1"), accountEntityProps("2")));

        // when
        List<AccountEntity> result = accountsResponse.getAccounts();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getResourceId()).isEqualTo("test-resource-id1");
        assertThat(result.get(1).getResourceId()).isEqualTo("test-resource-id2");
    }

    private Properties accountEntityProps(final String suffix) {
        Properties properties = new Properties();
        properties.setProperty("resourceId", "test-resource-id" + suffix);
        properties.setProperty("iban", "test-iban" + suffix);
        properties.setProperty("currency", "test-currency" + suffix);
        properties.setProperty("product", "test-product" + suffix);
        properties.setProperty("cashAccountType", "test-cash-account-type" + suffix);
        properties.setProperty("name", "test-name" + suffix);
        return properties;
    }

    private static AccountsResponse accountsAsResponse(final Collection<Properties> accounts) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(
                    "{\"accounts\":" + gsonObj.toJson(accounts) + "}", AccountsResponse.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
