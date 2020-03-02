package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.ACCOUNT_NUMBER_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createHmacAccountIds;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createHmacToken;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createTransactionsResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.converter.AmexTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AmexTransactionFetcherTest {

    private AmexTransactionFetcher amexTransactionFetcher;

    private AmexApiClient amexApiClientMock;

    private HmacAccountIdStorage hmacAccountIdStorageMock;

    private AmexTransactionalAccountConverter amexTransactionalAccountConverterMock;

    @Before
    public void setUp() {
        amexApiClientMock = mock(AmexApiClient.class);
        hmacAccountIdStorageMock = mock(HmacAccountIdStorage.class);
        amexTransactionalAccountConverterMock = mock(AmexTransactionalAccountConverter.class);

        amexTransactionFetcher =
                new AmexTransactionFetcher(
                        amexApiClientMock,
                        hmacAccountIdStorageMock,
                        amexTransactionalAccountConverterMock);
    }

    @Test
    public void shouldGetTransactions() {
        // given
        final HmacToken hmacToken = createHmacToken();

        final List<TransactionsResponseDto> transactionsResponse = createTransactionsResponse();
        when(amexApiClientMock.fetchTransactions(hmacToken)).thenReturn(transactionsResponse);

        final HmacAccountIds hmacAccountIds = createHmacAccountIds(hmacToken);
        when(hmacAccountIdStorageMock.get()).thenReturn(Optional.of(hmacAccountIds));

        final TransactionalAccount account = getTransactionalAccount();

        final List<AggregationTransaction> expectedResponse = createTransactions();
        when(amexTransactionalAccountConverterMock.convertResponseToAggregationTransactions(
                        transactionsResponse))
                .thenReturn(expectedResponse);

        // when
        final List<AggregationTransaction> response =
                amexTransactionFetcher.fetchTransactionsFor(account);

        // then
        verify(amexApiClientMock).fetchTransactions(hmacToken);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldThrowExceptionWhenHmacAccountIdWasNotFound() {
        // given
        when(hmacAccountIdStorageMock.get()).thenReturn(Optional.empty());

        final TransactionalAccount account = getTransactionalAccount();

        // when
        final Throwable thrown =
                catchThrowable(() -> amexTransactionFetcher.fetchTransactionsFor(account));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("No HmacAccountId found in the storage.");

        verify(amexApiClientMock, never()).fetchTransactions(any());
    }

    private static TransactionalAccount getTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(createExactCurrencyAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(ACCOUNT_NUMBER_1)
                                .withAccountNumber(ACCOUNT_NUMBER_1)
                                .withAccountName("account")
                                .addIdentifier(new IbanIdentifier(ACCOUNT_NUMBER_1))
                                .build())
                .build()
                .orElse(null);
    }

    private static List<AggregationTransaction> createTransactions() {
        return Collections.singletonList(
                Transaction.builder()
                        .setAmount(createExactCurrencyAmount())
                        .setDate(LocalDate.now())
                        .setDescription("description")
                        .setPending(false)
                        .build());
    }

    private static ExactCurrencyAmount createExactCurrencyAmount() {
        return new ExactCurrencyAmount(new BigDecimal(10.0), "GBP");
    }
}
