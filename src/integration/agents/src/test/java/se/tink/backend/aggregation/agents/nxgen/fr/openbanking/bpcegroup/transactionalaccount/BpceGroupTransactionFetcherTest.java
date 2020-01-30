package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class BpceGroupTransactionFetcherTest {

    private static final String RESOURCE_ID = "009988";
    private static final LocalDate DATE_FROM = LocalDate.of(2020,1, 1);
    private static final LocalDate DATE_TO = LocalDate.of(2020, 1, 2);

    private BpceGroupTransactionFetcher bpceGroupTransactionFetcher;

    private BpceGroupApiClient bpceGroupApiClientMock;

    @Before
    public void setUp() {
        bpceGroupApiClientMock = mock(BpceGroupApiClient.class);
        when(bpceGroupApiClientMock.getTransactions(RESOURCE_ID, DATE_FROM, DATE_TO)).thenReturn(getTransactionsResponse());

        bpceGroupTransactionFetcher = new BpceGroupTransactionFetcher(bpceGroupApiClientMock);
    }

    @Test
    public void shouldGetTransactionsFor() {
        //given
        final Date dateFrom = Date.from(DATE_FROM.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        final Date dateTo = Date.from(DATE_TO.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        final TransactionalAccount account = getTransactionalAccount();

        //when
        final PaginatorResponse response = bpceGroupTransactionFetcher.getTransactionsFor(account, dateFrom, dateTo);

        //then
        verify(bpceGroupApiClientMock).getTransactions(RESOURCE_ID, DATE_FROM, DATE_TO);
        verify(bpceGroupApiClientMock).recordCustomerConsent(any());
        assertThat(response).isNotNull();
        assertThat(response.getTinkTransactions()).hasSize(1);
    }

    private static TransactionsResponse getTransactionsResponse() {
    return SerializationUtils.deserializeFromString(
        "{\n"
            + "\"transactions\": [\n"
            + "    {\n"
            + "      \"resourceId\": \"" + RESOURCE_ID + "\",\n"
            + "      \"remittanceInformation\": [\n"
            + "        \"VIREMENT\"\n"
            + "      ],\n"
            + "      \"transactionAmount\": {\n"
            + "        \"amount\": \"27.00\",\n"
            + "        \"currency\": \"EUR\"\n"
            + "      },\n"
            + "      \"bookingDate\": \"2020-01-02\",\n"
            + "      \"valueDate\": \"2020-01-02\",\n"
            + "      \"transactionDate\": \"2020-01-02\",\n"
            + "      \"creditDebitIndicator\": \"CRDT\",\n"
            + "      \"entryReference\": \"\",\n"
            + "      \"status\": \"PDNG\"\n"
            + "    }\n"
            + "  ]\n"
            + "}",
        TransactionsResponse.class);
    }

    private static TransactionalAccount getTransactionalAccount() {
        final String accountNo = "7613807008043001965409135";
        final String iban = "FR" + accountNo;
        final ExactCurrencyAmount exactCurrencyAmount = new ExactCurrencyAmount(new BigDecimal(10.0), "EUR");

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(exactCurrencyAmount))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(accountNo)
                                .withAccountName("account")
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(RESOURCE_ID)
                .build()
                .orElse(null);
    }
}
