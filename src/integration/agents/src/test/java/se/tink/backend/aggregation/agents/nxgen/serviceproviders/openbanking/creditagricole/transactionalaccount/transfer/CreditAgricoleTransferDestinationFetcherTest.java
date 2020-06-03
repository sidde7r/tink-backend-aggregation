package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.BENEFICIARIES_2ND_PAGE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createAccount1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createAccount2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createBeneficiary1Account;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createBeneficiary2Account;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createGetTrustedBeneficiariesPage1Response;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createGetTrustedBeneficiariesPage2Response;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTrustedBeneficiariesResponse;
import se.tink.libraries.account.AccountIdentifier;

public class CreditAgricoleTransferDestinationFetcherTest {

    private CreditAgricoleTransferDestinationFetcher creditAgricoleTransferDestinationFetcher;

    private CreditAgricoleBaseApiClient creditAgricoleBaseApiClientMock;

    @Before
    public void setUp() {
        creditAgricoleBaseApiClientMock = mock(CreditAgricoleBaseApiClient.class);

        creditAgricoleTransferDestinationFetcher =
                new CreditAgricoleTransferDestinationFetcher(creditAgricoleBaseApiClientMock);
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // given
        final Account account1 = createAccount1();
        final Account account2 = createAccount2();
        final Collection<Account> accounts = Arrays.asList(account1, account2);

        final GetTrustedBeneficiariesResponse trustedBeneficiariesResponse1 =
                createGetTrustedBeneficiariesPage1Response();
        when(creditAgricoleBaseApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.of(trustedBeneficiariesResponse1));

        final GetTrustedBeneficiariesResponse trustedBeneficiariesResponse2 =
                createGetTrustedBeneficiariesPage2Response();
        when(creditAgricoleBaseApiClientMock.getTrustedBeneficiaries(BENEFICIARIES_2ND_PAGE_PATH))
                .thenReturn(Optional.of(trustedBeneficiariesResponse2));

        // when
        final TransferDestinationsResponse returnedResponse =
                creditAgricoleTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        assertThat(returnedResponse.getDestinations()).containsOnlyKeys(account1, account2);

        final AccountIdentifier identifier1 = account1.getIdentifier(AccountIdentifier.Type.IBAN);
        final AccountIdentifier identifier2 = account2.getIdentifier(AccountIdentifier.Type.IBAN);
        final AccountIdentifier beneficiaryIdentifier1 =
                createBeneficiary1Account().getIdentifier(AccountIdentifier.Type.IBAN);
        final AccountIdentifier beneficiaryIdentifier2 =
                createBeneficiary2Account().getIdentifier(AccountIdentifier.Type.IBAN);

        assertThat(getAccountIdentifiers(returnedResponse, account1))
                .containsExactlyInAnyOrder(
                        identifier2, beneficiaryIdentifier1, beneficiaryIdentifier2);
        assertThat(getAccountIdentifiers(returnedResponse, account2))
                .containsExactlyInAnyOrder(
                        identifier1, beneficiaryIdentifier1, beneficiaryIdentifier2);
    }

    @Test
    public void shouldFetchTransferDestinationsForEmptyBeneficiariesList() {
        // given
        final Account account1 = createAccount1();
        final Account account2 = createAccount2();
        final Collection<Account> accounts = Arrays.asList(account1, account2);

        when(creditAgricoleBaseApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.empty());

        // when
        final TransferDestinationsResponse returnedResponse =
                creditAgricoleTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        assertThat(returnedResponse.getDestinations()).containsOnlyKeys(account1, account2);

        final AccountIdentifier identifier1 = account1.getIdentifier(AccountIdentifier.Type.IBAN);
        final AccountIdentifier identifier2 = account2.getIdentifier(AccountIdentifier.Type.IBAN);

        assertThat(getAccountIdentifiers(returnedResponse, account1)).containsExactly(identifier2);
        assertThat(getAccountIdentifiers(returnedResponse, account2)).containsExactly(identifier1);
    }

    private static List<AccountIdentifier> getAccountIdentifiers(
            TransferDestinationsResponse transferDestinationsResponse, Account account) {
        return transferDestinationsResponse.getDestinations().get(account).stream()
                .map(TransferDestinationPattern::getAccountIdentifier)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
