package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.NEXT_PAGE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createAccounts;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createTrustedBeneficiariesPage1Response;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createTrustedBeneficiariesPage2Response;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestBase;

public class SocieteGeneraleTransferDestinationFetcherTest
        extends FrTransferDestinationFetcherTestBase {

    private SocieteGeneraleTransferDestinationFetcher societeGeneraleTransferDestinationFetcher;

    @Before
    public void setUp() {
        final SocieteGeneraleApiClient societeGeneraleApiClientMock =
                mock(SocieteGeneraleApiClient.class);

        when(societeGeneraleApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.of(createTrustedBeneficiariesPage1Response()));

        when(societeGeneraleApiClientMock.getTrustedBeneficiaries(NEXT_PAGE_PATH))
                .thenReturn(Optional.of(createTrustedBeneficiariesPage2Response()));

        societeGeneraleTransferDestinationFetcher =
                new SocieteGeneraleTransferDestinationFetcher(societeGeneraleApiClientMock);
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // given
        final List<Account> accounts = createAccounts();

        // when
        final TransferDestinationsResponse returnedResponse =
                societeGeneraleTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        verifyTransferDestinationsResponse(returnedResponse, accounts);
    }
}
