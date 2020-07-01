package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transfer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.BENEFICIARIES_2ND_PAGE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createAccounts;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createTrustedBeneficiariesPage1Response;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createTrustedBeneficiariesPage2Response;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestBase;

public class CmcicTransferDestinationFetcherTest extends FrTransferDestinationFetcherTestBase {

    private CmcicTransferDestinationFetcher cmcicTransferDestinationFetcher;

    private CmcicApiClient cmcicApiClientMock;

    @Before
    public void setUp() {
        cmcicApiClientMock = mock(CmcicApiClient.class);

        cmcicTransferDestinationFetcher = new CmcicTransferDestinationFetcher(cmcicApiClientMock);
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // given
        final List<Account> accounts = createAccounts();

        when(cmcicApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.of(createTrustedBeneficiariesPage1Response()));

        when(cmcicApiClientMock.getTrustedBeneficiaries(BENEFICIARIES_2ND_PAGE_PATH))
                .thenReturn(Optional.of(createTrustedBeneficiariesPage2Response()));

        // when
        final TransferDestinationsResponse returnedResponse =
                cmcicTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        verifyTransferDestinationsResponse(returnedResponse, accounts);
    }

    @Test
    public void shouldFetchTransferDestinationsForEmptyBeneficiariesList() {
        // given
        final List<Account> accounts = createAccounts();

        when(cmcicApiClientMock.getTrustedBeneficiaries()).thenReturn(Optional.empty());

        // when
        final TransferDestinationsResponse returnedResponse =
                cmcicTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        verifyTransferDestinationsResponseForEmptyBeneficiariesList(returnedResponse, accounts);
    }
}
