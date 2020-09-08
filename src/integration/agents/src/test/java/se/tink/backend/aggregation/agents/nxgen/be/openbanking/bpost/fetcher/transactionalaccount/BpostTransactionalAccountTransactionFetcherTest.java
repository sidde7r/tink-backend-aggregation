package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpostTransactionalAccountTransactionFetcherTest {

    private static final GetTransactionsResponse TRANSACTION_RESPONSE_1PAGE =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "    \"transactions\": {"
                            + "        \"booked\": ["
                            + "            {"
                            + "                \"transactionId\": \"C0H10AG03B0006R0~20200810~00001V\","
                            + "                \"bookingDate\": \"2020-08-10\","
                            + "                \"valueDate\": \"2020-08-10\","
                            + "                \"transactionAmount\": {"
                            + "                    \"currency\": \"EUR\","
                            + "                    \"amount\": -1.35"
                            + "                },"
                            + "                \"creditorName\": \"LIDL 243 WILLEBRWILLEBROEK\","
                            + "                \"creditorAccount\": {"
                            + "                    \"iban\": \" \""
                            + "                },"
                            + "                \"debtorAccount\": {"
                            + "                    \"iban\": \"***iban***\""
                            + "                },"
                            + "                \"remittanceInformationUnstructured\": \"20200808 12:42:46 ; Carte/Kaart : 0004313964830103 ;TermID : 01952683\","
                            + "                \"bankTransactionCode\": \"NTBKSY\","
                            + "                \"_links\": {"
                            + "                    \"transactionDetails\": {"
                            + "                        \"href\": \"***MASKED***/berlingroup/v1/accounts/000431396483/transactions/C0H10AG03B0006R0~20200810~00001V\""
                            + "                    }"
                            + "                }"
                            + "            },"
                            + "            {"
                            + "                \"transactionId\": \"C0H06AG03D000898~20200806~00002D\","
                            + "                \"bookingDate\": \"2020-08-06\","
                            + "                \"valueDate\": \"2020-08-06\","
                            + "                \"transactionAmount\": {"
                            + "                    \"currency\": \"EUR\","
                            + "                    \"amount\": -100.00"
                            + "                },"
                            + "                \"creditorName\": \"BELFIUS         BELFIUS350\","
                            + "                \"creditorAccount\": {"
                            + "                    \"iban\": \" \""
                            + "                },"
                            + "                \"debtorAccount\": {"
                            + "                    \"iban\": \"***iban***\""
                            + "                },"
                            + "                \"remittanceInformationUnstructured\": \"20200806 11:45:45 ; Carte/Kaart : 0004313964830103 ;TermID : 35018101\","
                            + "                \"bankTransactionCode\": \"ZPATM1\","
                            + "                \"_links\": {"
                            + "                    \"transactionDetails\": {"
                            + "                        \"href\": \"***MASKED***/berlingroup/v1/accounts/000431396483/transactions/C0H06AG03D000898~20200806~00002D\""
                            + "                    }"
                            + "                }"
                            + "            },"
                            + "            {"
                            + "                \"transactionId\": \"C0H05AG04B0006ZC~20200805~000007\","
                            + "                \"bookingDate\": \"2020-08-05\","
                            + "                \"valueDate\": \"2020-08-05\","
                            + "                \"transactionAmount\": {"
                            + "                    \"currency\": \"EUR\","
                            + "                    \"amount\": -43.13"
                            + "                },"
                            + "                \"creditorName\": \"3419 DATS24 GILLGILLY\","
                            + "                \"creditorAccount\": {"
                            + "                    \"iban\": \" \""
                            + "                },"
                            + "                \"debtorAccount\": {"
                            + "                    \"iban\": \"***iban***\""
                            + "                },"
                            + "                \"remittanceInformationUnstructured\": \"20200805 15:04:06 ; Carte/Kaart : 0004313964830103 ;TermID : 01618097\","
                            + "                \"bankTransactionCode\": \"NTBKSY\","
                            + "                \"_links\": {"
                            + "                    \"transactionDetails\": {"
                            + "                        \"href\": \"***MASKED***/berlingroup/v1/accounts/000431396483/transactions/C0H05AG04B0006ZC~20200805~000007\""
                            + "                    }"
                            + "                }"
                            + "            }"
                            + "        ],"
                            + "        \"pending\": [],"
                            + "        \"_links\": {"
                            + "            \"next\": {"
                            + "                \"href\": \"nextKey\""
                            + "            },"
                            + "            \"first\": {"
                            + "                \"href\": \"firstKey\""
                            + "            },"
                            + "            \"last\": {"
                            + "                \"href\": \"lastKey\""
                            + "            },"
                            + "            \"account\": {"
                            + "                \"href\": \"accountKey\""
                            + "            },"
                            + "            \"balances\": {"
                            + "                \"href\": \"balancesKey\""
                            + "            },"
                            + "            \"transactions\": {"
                            + "                \"href\": \"transactionsKey\""
                            + "            }"
                            + "        }"
                            + "    }"
                            + "}",
                    GetTransactionsResponse.class);

    private static final GetTransactionsResponse TRANSACTION_RESPONSE_2PAGE =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "    \"transactions\": {"
                            + "        \"booked\": ["
                            + "            {"
                            + "                \"transactionId\": \"C0H10AG03B0006R0~20200810~00001V\","
                            + "                \"bookingDate\": \"2020-08-10\","
                            + "                \"valueDate\": \"2020-08-10\","
                            + "                \"transactionAmount\": {"
                            + "                    \"currency\": \"EUR\","
                            + "                    \"amount\": -1.35"
                            + "                },"
                            + "                \"creditorName\": \"LIDL 243 WILLEBRWILLEBROEK\","
                            + "                \"creditorAccount\": {"
                            + "                    \"iban\": \" \""
                            + "                },"
                            + "                \"debtorAccount\": {"
                            + "                    \"iban\": \"***iban***\""
                            + "                },"
                            + "                \"remittanceInformationUnstructured\": \"20200808 12:42:46 ; Carte/Kaart : 0004313964830103 ;TermID : 01952683\","
                            + "                \"bankTransactionCode\": \"NTBKSY\","
                            + "                \"_links\": {"
                            + "                    \"transactionDetails\": {"
                            + "                        \"href\": \"***MASKED***/berlingroup/v1/accounts/000431396483/transactions/C0H10AG03B0006R0~20200810~00001V\""
                            + "                    }"
                            + "                }"
                            + "            },"
                            + "            {"
                            + "                \"transactionId\": \"C0H06AG03D000898~20200806~00002D\","
                            + "                \"bookingDate\": \"2020-08-06\","
                            + "                \"valueDate\": \"2020-08-06\","
                            + "                \"transactionAmount\": {"
                            + "                    \"currency\": \"EUR\","
                            + "                    \"amount\": -100.00"
                            + "                },"
                            + "                \"creditorName\": \"BELFIUS         BELFIUS350\","
                            + "                \"creditorAccount\": {"
                            + "                    \"iban\": \" \""
                            + "                },"
                            + "                \"debtorAccount\": {"
                            + "                    \"iban\": \"***iban***\""
                            + "                },"
                            + "                \"remittanceInformationUnstructured\": \"20200806 11:45:45 ; Carte/Kaart : 0004313964830103 ;TermID : 35018101\","
                            + "                \"bankTransactionCode\": \"ZPATM1\","
                            + "                \"_links\": {"
                            + "                    \"transactionDetails\": {"
                            + "                        \"href\": \"***MASKED***/berlingroup/v1/accounts/000431396483/transactions/C0H06AG03D000898~20200806~00002D\""
                            + "                    }"
                            + "                }"
                            + "            },"
                            + "            {"
                            + "                \"transactionId\": \"C0H05AG04B0006ZC~20200805~000007\","
                            + "                \"bookingDate\": \"2020-08-05\","
                            + "                \"valueDate\": \"2020-08-05\","
                            + "                \"transactionAmount\": {"
                            + "                    \"currency\": \"EUR\","
                            + "                    \"amount\": -43.13"
                            + "                },"
                            + "                \"creditorName\": \"3419 DATS24 GILLGILLY\","
                            + "                \"creditorAccount\": {"
                            + "                    \"iban\": \" \""
                            + "                },"
                            + "                \"debtorAccount\": {"
                            + "                    \"iban\": \"***iban***\""
                            + "                },"
                            + "                \"remittanceInformationUnstructured\": \"20200805 15:04:06 ; Carte/Kaart : 0004313964830103 ;TermID : 01618097\","
                            + "                \"bankTransactionCode\": \"NTBKSY\","
                            + "                \"_links\": {"
                            + "                    \"transactionDetails\": {"
                            + "                        \"href\": \"***MASKED***/berlingroup/v1/accounts/000431396483/transactions/C0H05AG04B0006ZC~20200805~000007\""
                            + "                    }"
                            + "                }"
                            + "            }"
                            + "        ],"
                            + "        \"pending\": [],"
                            + "        \"_links\": {"
                            + "            \"first\": {"
                            + "                \"href\": \"firstKey\""
                            + "            },"
                            + "            \"last\": {"
                            + "                \"href\": \"lastKey\""
                            + "            },"
                            + "            \"account\": {"
                            + "                \"href\": \"accountKey\""
                            + "            },"
                            + "            \"balances\": {"
                            + "                \"href\": \"balancesKey\""
                            + "            },"
                            + "            \"transactions\": {"
                            + "                \"href\": \"transactionsKey\""
                            + "            }"
                            + "        }"
                            + "    }"
                            + "}",
                    GetTransactionsResponse.class);

    private TransactionKeyWithInitDateFromFetcherController transactionDateFromFetcherController;
    private KeyWithInitiDateFromFetcher transactionKeyDateFromFetcher;
    private Account account;
    private CredentialsRequest credentialsRequest;
    private List<se.tink.backend.agents.rpc.Account> credentialsRequestAccounts;

    @Before
    public void init() {
        transactionKeyDateFromFetcher = Mockito.mock(KeyWithInitiDateFromFetcher.class);
        account = Mockito.mock(Account.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        credentialsRequestAccounts = new LinkedList<>();
        transactionDateFromFetcherController =
                new TransactionKeyWithInitDateFromFetcherController(
                        credentialsRequest, transactionKeyDateFromFetcher);
    }

    @Test
    public void shouldPaginateTransaction() {

        LocalDate fetcherCalculatedDateFrom = LocalDate.parse("2020-08-07");
        Mockito.when(transactionKeyDateFromFetcher.minimalFromDate())
                .thenReturn(fetcherCalculatedDateFrom);

        Mockito.when(
                        transactionKeyDateFromFetcher.fetchTransactionsFor(
                                account, fetcherCalculatedDateFrom))
                .thenReturn(TRANSACTION_RESPONSE_1PAGE);
        Mockito.when(transactionKeyDateFromFetcher.getTransactionsFor(account, "nextKey"))
                .thenReturn(TRANSACTION_RESPONSE_2PAGE);

        // when
        List<AggregationTransaction> result =
                transactionDateFromFetcherController.fetchTransactionsFor(account);

        // then
        Assert.assertTrue(result.size() == 6);
    }

    @Test
    public void shouldPassCorrectDateToArgument() {
        final String accountId = "1234567890";
        LocalDate accountCertainDate = LocalDate.parse("2020-06-01");
        Mockito.when(transactionKeyDateFromFetcher.minimalFromDate())
                .thenReturn(accountCertainDate);
        TransactionKeyPaginatorResponse transactions =
                Mockito.mock(TransactionKeyPaginatorResponse.class);
        AggregationTransaction expectedTransaction = Mockito.mock(AggregationTransaction.class);
        List<AggregationTransaction> transactionsList = Lists.newArrayList(expectedTransaction);
        Mockito.doReturn(transactionsList).when(transactions).getTinkTransactions();

        Mockito.when(
                        transactionKeyDateFromFetcher.fetchTransactionsFor(
                                account, accountCertainDate))
                .thenReturn(transactions);
        se.tink.backend.agents.rpc.Account aggregatedAccount =
                Mockito.mock(se.tink.backend.agents.rpc.Account.class);
        credentialsRequestAccounts.add(aggregatedAccount);
        Mockito.when(aggregatedAccount.getBankId()).thenReturn(accountId);
        Mockito.when(aggregatedAccount.getCertainDate())
                .thenReturn(
                        Date.from(
                                accountCertainDate
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant()));
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(credentialsRequestAccounts);

        // when
        List<AggregationTransaction> result =
                transactionDateFromFetcherController.fetchTransactionsFor(account);

        // then
        Mockito.verify(transactionKeyDateFromFetcher)
                .fetchTransactionsFor(account, accountCertainDate);
        Assert.assertTrue(result.size() == 1);
        Assert.assertTrue(result.get(0).equals(expectedTransaction));
    }

    @Test
    public void shouldReturnMinDateWhenCertainDateNotPresent() {

        final String accountId = "1234567890";
        LocalDate minDate = LocalDate.parse("2020-06-01");
        Mockito.when(transactionKeyDateFromFetcher.minimalFromDate()).thenReturn(minDate);
        TransactionKeyPaginatorResponse transactions =
                Mockito.mock(TransactionKeyPaginatorResponse.class);
        AggregationTransaction expectedTransaction = Mockito.mock(AggregationTransaction.class);
        List<AggregationTransaction> transactionsList = Lists.newArrayList(expectedTransaction);
        Mockito.doReturn(transactionsList).when(transactions).getTinkTransactions();

        Mockito.when(transactionKeyDateFromFetcher.fetchTransactionsFor(account, minDate))
                .thenReturn(transactions);
        se.tink.backend.agents.rpc.Account aggregatedAccount =
                Mockito.mock(se.tink.backend.agents.rpc.Account.class);
        credentialsRequestAccounts.add(aggregatedAccount);
        Mockito.when(aggregatedAccount.getBankId()).thenReturn(accountId);
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(credentialsRequestAccounts);

        // when
        List<AggregationTransaction> result =
                transactionDateFromFetcherController.fetchTransactionsFor(account);

        // then
        Mockito.verify(transactionKeyDateFromFetcher).fetchTransactionsFor(account, minDate);
        Assert.assertTrue(result.size() == 1);
        Assert.assertTrue(result.get(0).equals(expectedTransaction));
    }

    @Test
    public void shouldReturnMinDateWhenCertainDateBeforeMinDate() {
        final String accountId = "1234567890";
        LocalDate minDate = LocalDate.parse("2020-06-01");
        LocalDate accountCertainDate = LocalDate.parse("2020-05-01");
        Mockito.when(transactionKeyDateFromFetcher.minimalFromDate()).thenReturn(minDate);
        TransactionKeyPaginatorResponse transactions =
                Mockito.mock(TransactionKeyPaginatorResponse.class);
        AggregationTransaction expectedTransaction = Mockito.mock(AggregationTransaction.class);
        List<AggregationTransaction> transactionsList = Lists.newArrayList(expectedTransaction);
        Mockito.doReturn(transactionsList).when(transactions).getTinkTransactions();

        Mockito.when(transactionKeyDateFromFetcher.fetchTransactionsFor(account, minDate))
                .thenReturn(transactions);
        se.tink.backend.agents.rpc.Account aggregatedAccount =
                Mockito.mock(se.tink.backend.agents.rpc.Account.class);
        credentialsRequestAccounts.add(aggregatedAccount);
        Mockito.when(aggregatedAccount.getBankId()).thenReturn(accountId);
        Mockito.when(aggregatedAccount.getCertainDate())
                .thenReturn(
                        Date.from(
                                accountCertainDate
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant()));
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(credentialsRequestAccounts);

        // when
        List<AggregationTransaction> result =
                transactionDateFromFetcherController.fetchTransactionsFor(account);

        // then
        Mockito.verify(transactionKeyDateFromFetcher).fetchTransactionsFor(account, minDate);
        Assert.assertTrue(result.size() == 1);
        Assert.assertTrue(result.get(0).equals(expectedTransaction));
    }
}
