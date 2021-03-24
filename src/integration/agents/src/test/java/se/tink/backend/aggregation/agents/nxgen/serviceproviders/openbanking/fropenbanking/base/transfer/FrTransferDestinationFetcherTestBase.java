package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createBeneficiary1Account;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createBeneficiary2Account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Ignore
public abstract class FrTransferDestinationFetcherTestBase {

    protected static void verifyTransferDestinationsResponse(
            TransferDestinationsResponse returnedResponse, List<Account> accounts) {
        final Account account1 = accounts.get(0);
        final Account account2 = accounts.get(1);

        assertThat(returnedResponse.getDestinations()).containsOnlyKeys(account1, account2);

        final AccountIdentifier identifier1 = account1.getIdentifier(AccountIdentifierType.IBAN);
        final AccountIdentifier identifier2 = account2.getIdentifier(AccountIdentifierType.IBAN);
        final AccountIdentifier beneficiaryIdentifier1 =
                createBeneficiary1Account().getIdentifier(AccountIdentifierType.IBAN);
        final AccountIdentifier beneficiaryIdentifier2 =
                createBeneficiary2Account().getIdentifier(AccountIdentifierType.IBAN);

        assertThat(getAccountIdentifiers(returnedResponse, account1))
                .containsExactlyInAnyOrder(
                        identifier2, beneficiaryIdentifier1, beneficiaryIdentifier2);
        assertThat(getAccountIdentifiers(returnedResponse, account2))
                .containsExactlyInAnyOrder(
                        identifier1, beneficiaryIdentifier1, beneficiaryIdentifier2);
    }

    protected static void verifyTransferDestinationsResponseForEmptyBeneficiariesList(
            TransferDestinationsResponse returnedResponse, List<Account> accounts) {
        final Account account1 = accounts.get(0);
        final Account account2 = accounts.get(1);

        assertThat(returnedResponse.getDestinations()).containsOnlyKeys(account1, account2);

        final AccountIdentifier identifier1 = account1.getIdentifier(AccountIdentifierType.IBAN);
        final AccountIdentifier identifier2 = account2.getIdentifier(AccountIdentifierType.IBAN);

        assertThat(getAccountIdentifiers(returnedResponse, account1)).containsExactly(identifier2);
        assertThat(getAccountIdentifiers(returnedResponse, account2)).containsExactly(identifier1);
    }

    protected static List<AccountIdentifier> getAccountIdentifiers(
            TransferDestinationsResponse transferDestinationsResponse, Account account) {
        return transferDestinationsResponse.getDestinations().get(account).stream()
                .map(TransferDestinationPattern::getAccountIdentifier)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
