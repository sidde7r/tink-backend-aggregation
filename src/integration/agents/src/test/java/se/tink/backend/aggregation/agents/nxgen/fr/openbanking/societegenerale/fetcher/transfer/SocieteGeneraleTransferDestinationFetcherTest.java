package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.NEXT_PAGE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createAccount1;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createAccount2;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createBeneficiary1Account;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createBeneficiary2Account;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createTrustedBeneficiariesPage1Response;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createTrustedBeneficiariesPage2Response;

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
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc.TrustedBeneficiariesResponse;
import se.tink.libraries.account.AccountIdentifier;

public class SocieteGeneraleTransferDestinationFetcherTest {

    private SocieteGeneraleTransferDestinationFetcher societeGeneraleTransferDestinationFetcher;

    @Before
    public void setUp() {
        final SocieteGeneraleApiClient societeGeneraleApiClientMock =
                mock(SocieteGeneraleApiClient.class);
        final TrustedBeneficiariesResponse trustedBeneficiariesResponse1 =
                createTrustedBeneficiariesPage1Response();
        when(societeGeneraleApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.of(trustedBeneficiariesResponse1));

        final TrustedBeneficiariesResponse trustedBeneficiariesResponse2 =
                createTrustedBeneficiariesPage2Response();
        when(societeGeneraleApiClientMock.getTrustedBeneficiaries(NEXT_PAGE_PATH))
                .thenReturn(Optional.of(trustedBeneficiariesResponse2));

        societeGeneraleTransferDestinationFetcher =
                new SocieteGeneraleTransferDestinationFetcher(societeGeneraleApiClientMock);
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // given
        final Account account1 = createAccount1();
        final Account account2 = createAccount2();
        final Collection<Account> accounts = Arrays.asList(account1, account2);

        // when
        final TransferDestinationsResponse returnedResponse =
                societeGeneraleTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

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

    private static List<AccountIdentifier> getAccountIdentifiers(
            TransferDestinationsResponse transferDestinationsResponse, Account account) {
        return transferDestinationsResponse.getDestinations().get(account).stream()
                .map(TransferDestinationPattern::getAccountIdentifier)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
