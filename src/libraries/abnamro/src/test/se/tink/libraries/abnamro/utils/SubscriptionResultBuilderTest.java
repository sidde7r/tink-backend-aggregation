package se.tink.libraries.abnamro.utils;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import se.tink.libraries.abnamro.client.model.RejectedContractEntity;
import se.tink.libraries.abnamro.client.model.SubscriptionResult;
import se.tink.backend.core.Account;
import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionResultBuilderTest {

    @Test(expected = NullPointerException.class)
    public void testExceptionIfAccountsIsNull() {

        SubscriptionResultBuilder builder = new SubscriptionResultBuilder();

        builder.withAccounts(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void testExceptionIfRejectedContractsIsNull() {

        SubscriptionResultBuilder builder = new SubscriptionResultBuilder();

        builder.withRejectedContracts(null).build();
    }

    @Test
    public void testNoInput() {

        SubscriptionResultBuilder builder = new SubscriptionResultBuilder();

        List<Account> account = ImmutableList.of();
        List<RejectedContractEntity> rejectedContracts = ImmutableList.of();

        SubscriptionResult result = builder.withAccounts(account).withRejectedContracts(rejectedContracts).build();

        assertThat(result.getSubscribedAccounts()).isEmpty();
        assertThat(result.getRejectedAccounts()).isEmpty();
    }

    @Test
    public void testAllAccountsSubscribed() {

        SubscriptionResultBuilder builder = new SubscriptionResultBuilder();

        Account account1 = new Account();
        account1.setBankId("1");

        List<Account> accounts = ImmutableList.of(account1);
        List<RejectedContractEntity> rejectedContracts = ImmutableList.of();

        SubscriptionResult result = builder.withAccounts(accounts).withRejectedContracts(rejectedContracts).build();

        assertThat(result.getSubscribedAccounts()).containsExactly(account1);
        assertThat(result.getRejectedAccounts()).isEmpty();
    }

    @Test
    public void testAccountsSubscribedAndAccountsRejected() {

        SubscriptionResultBuilder builder = new SubscriptionResultBuilder();

        Account account1 = new Account();
        account1.setBankId("1");

        Account account2 = new Account();
        account2.setBankId("2");

        RejectedContractEntity rejected1 = new RejectedContractEntity();
        rejected1.setContractNumber(1L);
        rejected1.setRejectedReasonCode(1);

        List<Account> accounts = ImmutableList.of(account1, account2);
        List<RejectedContractEntity> rejectedContracts = ImmutableList.of(rejected1);

        SubscriptionResult result = builder.withAccounts(accounts).withRejectedContracts(rejectedContracts).build();

        assertThat(result.getSubscribedAccounts()).containsExactly(account2);
        assertThat(result.getRejectedAccounts()).containsExactly(account1);
    }

}
