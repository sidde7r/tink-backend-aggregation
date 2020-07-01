package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer;

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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestBase;

public class CreditAgricoleTransferDestinationFetcherTest
        extends FrTransferDestinationFetcherTestBase {

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
        final List<Account> accounts = createAccounts();

        when(creditAgricoleBaseApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.of(createTrustedBeneficiariesPage1Response()));

        when(creditAgricoleBaseApiClientMock.getTrustedBeneficiaries(BENEFICIARIES_2ND_PAGE_PATH))
                .thenReturn(Optional.of(createTrustedBeneficiariesPage2Response()));

        // when
        final TransferDestinationsResponse returnedResponse =
                creditAgricoleTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        verifyTransferDestinationsResponse(returnedResponse, accounts);
    }

    @Test
    public void shouldFetchTransferDestinationsForEmptyBeneficiariesList() {
        // given
        final List<Account> accounts = createAccounts();

        when(creditAgricoleBaseApiClientMock.getTrustedBeneficiaries())
                .thenReturn(Optional.empty());

        // when
        final TransferDestinationsResponse returnedResponse =
                creditAgricoleTransferDestinationFetcher.fetchTransferDestinationsFor(accounts);

        // then
        verifyTransferDestinationsResponseForEmptyBeneficiariesList(returnedResponse, accounts);
    }
}
