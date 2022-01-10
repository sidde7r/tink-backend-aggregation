package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.ReservationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SebKortTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/sebkort/fetcher/resources";

    private SebKortApiClient spyClient;
    private SebKortTransactionFetcher fetcher;

    @Before
    public void setUp() {
        TinkHttpClient client = mock(TinkHttpClient.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        SebKortConfiguration configuration = mock(SebKortConfiguration.class);
        SebKortApiClient apiClient = new SebKortApiClient(client, sessionStorage, configuration);
        spyClient = spy(apiClient);
        fetcher = new SebKortTransactionFetcher(spyClient);
    }

    @Test
    public void shouldNotHaveDuplicatesAlthoughTrxListHasDuplicates() throws ParseException {
        CreditCardAccount account = getSampleCreditCardAccount();
        Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-07-08");
        Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-06");

        ReservationsResponse reservationsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "reservationForContractId.json").toFile(),
                        ReservationsResponse.class);

        TransactionsResponse transactionsCardAccountId =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactionsForCardAccountId.json").toFile(),
                        TransactionsResponse.class);

        TransactionsResponse transactionsCardContractId =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactionsForCardContractId.json").toFile(),
                        TransactionsResponse.class);

        // 0 reservations
        doReturn(reservationsResponse).when(spyClient).fetchReservationsForContractId("IDENTIFIER");

        // 2 trx, one is duplicated with trx from transactionsCardContractId
        doReturn(transactionsCardAccountId)
                .when(spyClient)
                .fetchTransactionsForCardAccountId("CARD_ACCOUNT_ID", fromDate, toDate);

        // 7 trx, one is duplicated with trx from transactionsCardAccountId (PURCHASE)
        // Only 3 trx is of type PAYMENT and will not be filtered out
        doReturn(transactionsCardContractId)
                .when(spyClient)
                .fetchTransactionsForContractId("IDENTIFIER", fromDate, toDate);

        PaginatorResponse result = fetcher.getTransactionsFor(account, fromDate, toDate);

        // Only 5 trx remain
        Assert.assertEquals(5, result.getTinkTransactions().size());
    }

    private CreditCardAccount getSampleCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber("1234")
                                .withBalance(ExactCurrencyAmount.inSEK(0))
                                .withAvailableCredit(ExactCurrencyAmount.inSEK(0))
                                .withCardAlias("CARD_ALIAS")
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("UNIQUE_IDENTIFIER")
                                .withAccountNumber("ACCOUNT_NUMBER")
                                .withAccountName("ACCOUNT_NAME")
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.BBAN, "BBAN"))
                                .build())
                .setApiIdentifier("IDENTIFIER")
                .putInTemporaryStorage(SebKortConstants.StorageKey.IS_ACCOUNT_OWNER, true)
                .putInTemporaryStorage(StorageKey.CARD_ACCOUNT_ID, "CARD_ACCOUNT_ID")
                .build();
    }
}
