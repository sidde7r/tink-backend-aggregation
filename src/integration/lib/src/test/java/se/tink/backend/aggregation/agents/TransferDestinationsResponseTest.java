package se.tink.backend.aggregation.agents;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class TransferDestinationsResponseTest {
    @Test
    public void destinationsIsInitializedEmpty() {
        TransferDestinationsResponse response = new TransferDestinationsResponse();
        assertThat(response.getDestinations()).isNotNull();
    }

    @Test
    public void addSingleAccountDestination() {
        Account account = createAccountWithBankId("12345");
        TransferDestinationPattern destination = createTransferDestinationPattern(1);

        TransferDestinationsResponse response = new TransferDestinationsResponse();
        response.addDestination(account, destination);

        assertThat(response.getDestinations()).hasSize(1).containsKey(account);
        assertThat(response.getDestinations().get(account)).hasSize(1).contains(destination);
    }

    @Test
    public void addAccountDestinations() {
        Account account = createAccountWithBankId("12345");
        TransferDestinationPattern destination1 = createTransferDestinationPattern(1);
        TransferDestinationPattern destination2 = createTransferDestinationPattern(2);

        TransferDestinationsResponse response =
                new TransferDestinationsResponse(
                        account, ImmutableList.of(destination1, destination2));

        assertThat(response.getDestinations()).hasSize(1).containsKey(account);
        assertThat(response.getDestinations().get(account))
                .hasSize(2)
                .contains(destination1, destination2);
    }

    @Test
    public void addAccountDestinations_withTwoCallsForSameAccount() {
        Account account = createAccountWithBankId("12345");
        TransferDestinationPattern destination1 = createTransferDestinationPattern(1);
        TransferDestinationPattern destination2 = createTransferDestinationPattern(2);

        TransferDestinationsResponse response = new TransferDestinationsResponse();
        response.addDestinations(account, ImmutableList.of(destination1));
        response.addDestinations(account, ImmutableList.of(destination2));

        assertThat(response.getDestinations()).hasSize(1).containsKey(account);
        assertThat(response.getDestinations().get(account))
                .hasSize(2)
                .contains(destination1, destination2);
    }

    @Test
    public void addAccountsAndDestinations() {
        Account account1 = createAccountWithBankId("12345");
        Account account2 = createAccountWithBankId("56789");

        TransferDestinationPattern destination1 = createTransferDestinationPattern(1);
        TransferDestinationPattern destination2 = createTransferDestinationPattern(2);

        ImmutableMap<Account, List<TransferDestinationPattern>> accountDestinations =
                ImmutableMap.<Account, List<TransferDestinationPattern>>builder()
                        .put(account1, ImmutableList.of(destination1, destination2))
                        .put(account2, ImmutableList.of(destination2))
                        .build();

        TransferDestinationsResponse response =
                new TransferDestinationsResponse(accountDestinations);

        assertThat(response.getDestinations()).hasSize(2).containsKeys(account1, account2);
        assertThat(response.getDestinations().get(account1))
                .hasSize(2)
                .contains(destination1, destination2);
        assertThat(response.getDestinations().get(account2)).hasSize(1).contains(destination2);
    }

    @Test
    public void addAccountsAndDestinations_withTwoCalls() {
        Account account1 = createAccountWithBankId("12345");
        Account account2 = createAccountWithBankId("56789");

        TransferDestinationPattern destination1 = createTransferDestinationPattern(1);
        TransferDestinationPattern destination2 = createTransferDestinationPattern(2);

        TransferDestinationsResponse response =
                new TransferDestinationsResponse(
                        account1, ImmutableList.of(destination1, destination2));
        response.addDestinations(account2, ImmutableList.of(destination2));

        assertThat(response.getDestinations()).hasSize(2).containsKeys(account1, account2);
        assertThat(response.getDestinations().get(account1))
                .hasSize(2)
                .contains(destination1, destination2);
        assertThat(response.getDestinations().get(account2)).hasSize(1).contains(destination2);
    }

    private static TransferDestinationPattern createTransferDestinationPattern(int pattern) {
        return TransferDestinationPattern.createForMultiMatch(
                AccountIdentifierType.SE, "" + pattern);
    }

    private static Account createAccountWithBankId(String bankId) {
        Account account = new Account();

        account.setBankId(bankId);

        return account;
    }
}
