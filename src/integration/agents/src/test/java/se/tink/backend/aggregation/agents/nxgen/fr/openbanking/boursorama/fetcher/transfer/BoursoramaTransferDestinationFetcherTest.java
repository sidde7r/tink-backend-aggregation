package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaTestFixtures.createBeneficiaryAccount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaTestFixtures.createGetTrustedBeneficiariesResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createAccount1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createAccount2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestBase;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class BoursoramaTransferDestinationFetcherTest extends FrTransferDestinationFetcherTestBase {

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

        final AccountIdentifier identifier1 = account1.getIdentifier(AccountIdentifierType.IBAN);
        final AccountIdentifier identifier2 = account2.getIdentifier(AccountIdentifierType.IBAN);
        final AccountIdentifier beneficiaryIdentifier =
                createBeneficiaryAccount().getIdentifier(AccountIdentifierType.IBAN);

        assertThat(getAccountIdentifiers(returnedResponse, account1))
                .containsExactlyInAnyOrder(identifier2, beneficiaryIdentifier);
        assertThat(getAccountIdentifiers(returnedResponse, account2))
                .containsExactlyInAnyOrder(identifier1, beneficiaryIdentifier);
    }
}
