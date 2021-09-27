package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetAccountsResponseTest {

    @Test
    public void shouldReturnEmptyAccountsListWhenEmptyAccountsResponse() {
        // given
        GetAccountsResponse accountsResponse = new GetAccountsResponse();
        List<AccountEntity> accountEntities = new ArrayList<>();
        accountEntities.add(getEmptyAccountEntity());
        ReflectionTestUtils.setField(accountsResponse, "accounts", accountEntities);

        // then
        assertThatThrownBy(() -> accountsResponse.toTinkAccounts())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No balance found");
    }

    private AccountEntity getEmptyAccountEntity() {
        return SerializationUtils.deserializeFromString("{}", AccountEntity.class);
    }
}
