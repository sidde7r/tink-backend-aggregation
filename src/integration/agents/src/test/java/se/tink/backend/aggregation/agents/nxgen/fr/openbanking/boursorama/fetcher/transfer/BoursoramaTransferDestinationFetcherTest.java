package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaTestFixtures.createAccount1;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaTestFixtures.createAccount2;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaTestFixtures.createBeneficiaryAccount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaTestFixtures.createGetTrustedBeneficiariesResponse;

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
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.libraries.account.AccountIdentifier;

public class BoursoramaTransferDestinationFetcherTest {

    private BoursoramaTransferDestinationFetcher boursoramaTransferDestinationFetcher;

    private BoursoramaApiClient boursoramaApiClientMock;

    @Before
    public void setUp() {
        boursoramaApiClientMock = mock(BoursoramaApiClient.class);

        boursoramaTransferDestinationFetcher =
                new BoursoramaTransferDestinationFetcher(boursoramaApiClientMock);
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // given
        final Account account1 = createAccount1();
        final Account account2 = createAccount2();
        final Collection<Account> accounts = Arrays.asList(account1, account2);

        final TrustedBeneficiariesResponseDto trustedBeneficiariesResponse =
                createGetTrustedBeneficiariesResponse();
        when(boursoramaApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.of(trustedBeneficiariesResponse));

        // when
        final TransferDestinationsResponse returnedResponse =
                boursoramaTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        assertThat(returnedResponse.getDestinations()).containsOnlyKeys(account1, account2);

        final AccountIdentifier identifier1 = account1.getIdentifier(AccountIdentifier.Type.IBAN);
        final AccountIdentifier identifier2 = account2.getIdentifier(AccountIdentifier.Type.IBAN);
        final AccountIdentifier beneficiaryIdentifier =
                createBeneficiaryAccount().getIdentifier(AccountIdentifier.Type.IBAN);

        assertThat(getAccountIdentifiers(returnedResponse, account1))
                .containsExactlyInAnyOrder(identifier2, beneficiaryIdentifier);
        assertThat(getAccountIdentifiers(returnedResponse, account2))
                .containsExactlyInAnyOrder(identifier1, beneficiaryIdentifier);
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
