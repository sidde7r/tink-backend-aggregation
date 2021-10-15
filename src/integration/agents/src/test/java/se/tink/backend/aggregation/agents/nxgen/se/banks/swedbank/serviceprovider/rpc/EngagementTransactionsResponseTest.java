package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EngagementTransactionsResponseTest {

    @Test
    public void shouldReturnOptionalTrueWhenCanFetchMore() {

        EngagementTransactionsResponse engagementTransactionsResponse =
                SerializationUtils.deserializeFromString(
                        "{\"moreTransactionsAvailable\":true,\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/xlsx/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"CSV\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/csv/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDF\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdf/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"TXT\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/txt/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDFVIEW\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdfview/de525608fd7c11eaadc10242ac1200020000002b\"}}}],\"totalTransactionsCount\":0,\"account\":{\"availableAmount\":\"100 000,00\",\"antalTransaktionerTotalt\":96,\"currencyAccount\":false,\"internalAccount\":false,\"bic\":\"SWEDSESS\",\"iban\":\"SE11 1111 1111 1111 1111 1111\",\"accountAuthorizations\":{\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/accounts/de525608fd7c11eaadc10242ac1200020000002b/authorizations\"}}},\"availableForFavouriteAccount\":false,\"availableForPriorityAccount\":false,\"id\":\"de525608fd7c11eaadc10242ac1200020000002b\",\"name\":\"Företagskonto\",\"accountNumber\":\"111 111 111-1\",\"clearingNumber\":\"1111-1\",\"fullyFormattedNumber\":\"1111-1,111 111 111-1\",\"nonFormattedNumber\":\"111111111111111\",\"balance\":\"100 000,00\",\"currency\":\"SEK\",\"expenseControl\":{\"status\":\"UNAVAILABLE\"},\"originalName\":\"Företagskonto\"},\"transactions\":[],\"reservedTransactions\":[],\"numberOfTransactions\":0,\"numberOfReservedTransactions\":0,\"numberOfBankGiroPrognosisTransactions\":1,\"bankGiroPrognosisTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10 000,00\",\"description\":\"Ingår ej i tillgängligt belopp\",\"currency\":\"SEK\",\"bgprognosTypeDescription\":\"Bg prognos.\",\"bgprognosType\":\"DEFAULT\"}",
                        EngagementTransactionsResponse.class);

        Optional<Boolean> result = engagementTransactionsResponse.canFetchMore();

        assertEquals(Optional.of(true), result);
    }

    @Test
    public void shouldReturnFalseIfCannotFetchMore() {

        EngagementTransactionsResponse engagementTransactionsResponse =
                SerializationUtils.deserializeFromString(
                        "{\"moreTransactionsAvailable\":false,\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/xlsx/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"CSV\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/csv/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDF\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdf/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"TXT\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/txt/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDFVIEW\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdfview/de525608fd7c11eaadc10242ac1200020000002b\"}}}],\"totalTransactionsCount\":0,\"account\":{\"availableAmount\":\"100 000,00\",\"antalTransaktionerTotalt\":96,\"currencyAccount\":false,\"internalAccount\":false,\"bic\":\"SWEDSESS\",\"iban\":\"SE11 1111 1111 1111 1111 1111\",\"accountAuthorizations\":{\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/accounts/de525608fd7c11eaadc10242ac1200020000002b/authorizations\"}}},\"availableForFavouriteAccount\":false,\"availableForPriorityAccount\":false,\"id\":\"de525608fd7c11eaadc10242ac1200020000002b\",\"name\":\"Företagskonto\",\"accountNumber\":\"111 111 111-1\",\"clearingNumber\":\"1111-1\",\"fullyFormattedNumber\":\"1111-1,111 111 111-1\",\"nonFormattedNumber\":\"111111111111111\",\"balance\":\"100 000,00\",\"currency\":\"SEK\",\"expenseControl\":{\"status\":\"UNAVAILABLE\"},\"originalName\":\"Företagskonto\"},\"transactions\":[],\"reservedTransactions\":[],\"numberOfTransactions\":0,\"numberOfReservedTransactions\":0,\"numberOfBankGiroPrognosisTransactions\":1,\"bankGiroPrognosisTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10 000,00\",\"description\":\"Ingår ej i tillgängligt belopp\",\"currency\":\"SEK\",\"bgprognosTypeDescription\":\"Bg prognos.\",\"bgprognosType\":\"DEFAULT\"}",
                        EngagementTransactionsResponse.class);

        Optional<Boolean> result = engagementTransactionsResponse.canFetchMore();

        assertEquals(Optional.of(false), result);
    }

    @Test
    public void shouldReturnFalseWhenGetNextIsNotValid() {

        EngagementTransactionsResponse engagementTransactionsResponse =
                SerializationUtils.deserializeFromString(
                        "{\"moreTransactionsAvailable\":false,\"links\":{\"next\":{\"method\":\"\",\"uri\":\"/v5/engagement/transactions/xlsx/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"CSV\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/csv/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDF\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdf/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"TXT\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/txt/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDFVIEW\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdfview/de525608fd7c11eaadc10242ac1200020000002b\"}}}],\"totalTransactionsCount\":0,\"account\":{\"availableAmount\":\"100 000,00\",\"antalTransaktionerTotalt\":96,\"currencyAccount\":false,\"internalAccount\":false,\"bic\":\"SWEDSESS\",\"iban\":\"SE11 1111 1111 1111 1111 1111\",\"accountAuthorizations\":{\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/accounts/de525608fd7c11eaadc10242ac1200020000002b/authorizations\"}}},\"availableForFavouriteAccount\":false,\"availableForPriorityAccount\":false,\"id\":\"de525608fd7c11eaadc10242ac1200020000002b\",\"name\":\"Företagskonto\",\"accountNumber\":\"111 111 111-1\",\"clearingNumber\":\"1111-1\",\"fullyFormattedNumber\":\"1111-1,111 111 111-1\",\"nonFormattedNumber\":\"111111111111111\",\"balance\":\"100 000,00\",\"currency\":\"SEK\",\"expenseControl\":{\"status\":\"UNAVAILABLE\"},\"originalName\":\"Företagskonto\"},\"transactions\":[],\"reservedTransactions\":[],\"numberOfTransactions\":0,\"numberOfReservedTransactions\":0,\"numberOfBankGiroPrognosisTransactions\":1,\"bankGiroPrognosisTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10 000,00\",\"description\":\"Ingår ej i tillgängligt belopp\",\"currency\":\"SEK\",\"bgprognosTypeDescription\":\"Bg prognos.\",\"bgprognosType\":\"DEFAULT\"}",
                        EngagementTransactionsResponse.class);

        Optional<Boolean> result = engagementTransactionsResponse.canFetchMore();

        assertEquals(Optional.of(false), result);
    }

    @Test
    public void shouldMapReservedTransactions() {

        EngagementTransactionsResponse engagementTransactionsResponse =
                SerializationUtils.deserializeFromString(
                        "{\"statementTimestamp\":\"2020-09-23T08:38:58+0200\",\"export\":[{\"format\":\"XLSX\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/xlsx/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"CSV\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/csv/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDF\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdf/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"TXT\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/txt/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDFVIEW\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdfview/de525608fd7c11eaadc10242ac1200020000002b\"}}}],\"totalTransactionsCount\":0,\"account\":{\"availableAmount\":\"100 000,00\",\"antalTransaktionerTotalt\":3,\"currencyAccount\":false,\"internalAccount\":false,\"bic\":\"SWEDSESS\",\"iban\":\"SE11 1111 1111 1111 1111 1111\",\"accountAuthorizations\":{\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/accounts/de525608fd7c11eaadc10242ac1200020000002b/authorizations\"}}},\"availableForFavouriteAccount\":false,\"availableForPriorityAccount\":false,\"id\":\"de525608fd7c11eaadc10242ac1200020000002b\",\"name\":\"Företagskonto\",\"accountNumber\":\"111 111 111-1\",\"clearingNumber\":\"1111-1\",\"fullyFormattedNumber\":\"1111-1,111 111 111-1\",\"nonFormattedNumber\":\"111111111111111\",\"balance\":\"100 000,00\",\"currency\":\"SEK\",\"expenseControl\":{\"status\":\"UNAVAILABLE\"},\"originalName\":\"Företagskonto\"},\"transactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10,00\",\"description\":\"Betalning\",\"currency\":\"SEK\",\"expenseControlIncluded\":\"UNAVAILABLE\",\"details\":{\"transactionType\":\"Insättning\",\"reference\":\"12324345345345\",\"bankReference\":\"12324345345345\",\"message\":\"12324345345345\",\"transactionDate\":\"2020-09-23\",\"bookedDate\":\"2020-09-23\"},\"accountingDate\":\"2020-09-23\",\"accountingBalance\":{\"amount\":\"100 000,00\",\"currencyCode\":\"SEK\"},\"bookedDate\":\"2020-09-23\"},{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"-1,50\",\"description\":\"Pris betalning\",\"currency\":\"SEK\",\"expenseControlIncluded\":\"UNAVAILABLE\",\"details\":{\"transactionType\":\"Uttag\",\"reference\":\"12324345345345\",\"bankReference\":\"12324345345345\",\"message\":\"12324345345345\",\"transactionDate\":\"2020-09-23\",\"bookedDate\":\"2020-09-23\"},\"accountingDate\":\"2020-09-23\",\"accountingBalance\":{\"amount\":\"100 000,00\",\"currencyCode\":\"SEK\"},\"bookedDate\":\"2020-09-23\"},{\"categoryId\":0,\"date\":\"2020-09-21\",\"amount\":\"-1 000,00\",\"description\":\"Kortköp/uttag\",\"currency\":\"SEK\",\"expenseControlIncluded\":\"UNAVAILABLE\",\"details\":{\"transactionType\":\"Uttag\",\"reference\":\"Market place\",\"bankReference\":\"123434werewr\",\"message\":\"Market place\",\"valueDate\":\"2020-09-21\",\"transactionDate\":\"2020-09-21\",\"bookedDate\":\"2020-09-21\"},\"accountingDate\":\"2020-09-21\",\"accountingBalance\":{\"amount\":\"100 000,00\",\"currencyCode\":\"SEK\"},\"bookedDate\":\"2020-09-21\"}],\"reservedTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-22\",\"amount\":\"-500,00\",\"description\":\"Skyddat belopp\",\"currency\":\"SEK\",\"accountingDate\":\"SEK\"},{\"hasUncertainCategorization\":false,\"accountId\":0,\"reservationId\":\"0000000012345678\",\"comments\":[],\"id\":162686835,\"date\":\"2020-09-22T00:00:00\",\"dateFormatted\":\"2020-09-22\",\"amount\":-50.0,\"currency\":\"SEK\",\"text\":\"Recent purchase\",\"categoryId\":289,\"detectedCategories\":[{\"categoryId\":289,\"score\":1.0}],\"isOwnAccountTransfer\":false}],\"moreTransactionsAvailable\":true,\"numberOfTransactions\":3,\"numberOfReservedTransactions\":2,\"numberOfBankGiroPrognosisTransactions\":1,\"bankGiroPrognosisTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10 000,00\",\"description\":\"Ingår ej i tillgängligt belopp\",\"currency\":\"SEK\",\"bgprognosTypeDescription\":\"Bg prognos.\",\"bgprognosType\":\"DEFAULT\"}],\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/de525608fd7c11eaadc10242ac1200020000002b?page=next&paginationId=de525608fd7c11eaadc10242ac1200020000002b\"}}}\n",
                        EngagementTransactionsResponse.class);

        List<Transaction> result =
                engagementTransactionsResponse.reservedTransactionsToTransactions();

        assertEquals(2, result.size());
    }

    @Test
    public void shouldMapTransactions() {

        EngagementTransactionsResponse engagementTransactionsResponse =
                SerializationUtils.deserializeFromString(
                        "{\"statementTimestamp\":\"2020-09-23T08:38:58+0200\",\"export\":[{\"format\":\"XLSX\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/xlsx/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"CSV\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/csv/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDF\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdf/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"TXT\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/txt/de525608fd7c11eaadc10242ac1200020000002b\"}}},{\"format\":\"PDFVIEW\",\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/pdfview/de525608fd7c11eaadc10242ac1200020000002b\"}}}],\"totalTransactionsCount\":0,\"account\":{\"availableAmount\":\"100 000,00\",\"antalTransaktionerTotalt\":3,\"currencyAccount\":false,\"internalAccount\":false,\"bic\":\"SWEDSESS\",\"iban\":\"SE11 1111 1111 1111 1111 1111\",\"accountAuthorizations\":{\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/accounts/de525608fd7c11eaadc10242ac1200020000002b/authorizations\"}}},\"availableForFavouriteAccount\":false,\"availableForPriorityAccount\":false,\"id\":\"de525608fd7c11eaadc10242ac1200020000002b\",\"name\":\"Företagskonto\",\"accountNumber\":\"111 111 111-1\",\"clearingNumber\":\"1111-1\",\"fullyFormattedNumber\":\"1111-1,111 111 111-1\",\"nonFormattedNumber\":\"111111111111111\",\"balance\":\"100 000,00\",\"currency\":\"SEK\",\"expenseControl\":{\"status\":\"UNAVAILABLE\"},\"originalName\":\"Företagskonto\"},\"transactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10,00\",\"description\":\"Betalning\",\"currency\":\"SEK\",\"expenseControlIncluded\":\"UNAVAILABLE\",\"details\":{\"transactionType\":\"Insättning\",\"reference\":\"12324345345345\",\"bankReference\":\"12324345345345\",\"message\":\"12324345345345\",\"transactionDate\":\"2020-09-23\",\"bookedDate\":\"2020-09-23\"},\"accountingDate\":\"2020-09-23\",\"accountingBalance\":{\"amount\":\"100 000,00\",\"currencyCode\":\"SEK\"},\"bookedDate\":\"2020-09-23\"},{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"-1,50\",\"description\":\"Pris betalning\",\"currency\":\"SEK\",\"expenseControlIncluded\":\"UNAVAILABLE\",\"details\":{\"transactionType\":\"Uttag\",\"reference\":\"12324345345345\",\"bankReference\":\"12324345345345\",\"message\":\"12324345345345\",\"transactionDate\":\"2020-09-23\",\"bookedDate\":\"2020-09-23\"},\"accountingDate\":\"2020-09-23\",\"accountingBalance\":{\"amount\":\"100 000,00\",\"currencyCode\":\"SEK\"},\"bookedDate\":\"2020-09-23\"},{\"categoryId\":0,\"date\":\"2020-09-21\",\"amount\":\"-1 000,00\",\"description\":\"Kortköp/uttag\",\"currency\":\"SEK\",\"expenseControlIncluded\":\"UNAVAILABLE\",\"details\":{\"transactionType\":\"Uttag\",\"reference\":\"Market place\",\"bankReference\":\"123434werewr\",\"message\":\"Market place\",\"valueDate\":\"2020-09-21\",\"transactionDate\":\"2020-09-21\",\"bookedDate\":\"2020-09-21\"},\"accountingDate\":\"2020-09-21\",\"accountingBalance\":{\"amount\":\"100 000,00\",\"currencyCode\":\"SEK\"},\"bookedDate\":\"2020-09-21\"}],\"reservedTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-22\",\"amount\":\"-500,00\",\"description\":\"Skyddat belopp\",\"currency\":\"SEK\",\"accountingDate\":\"SEK\"},{\"hasUncertainCategorization\":false,\"accountId\":0,\"reservationId\":\"0000000012345678\",\"comments\":[],\"id\":162686835,\"date\":\"2020-09-22T00:00:00\",\"dateFormatted\":\"2020-09-22\",\"amount\":-50.0,\"currency\":\"SEK\",\"text\":\"Recent purchase\",\"categoryId\":289,\"detectedCategories\":[{\"categoryId\":289,\"score\":1.0}],\"isOwnAccountTransfer\":false}],\"moreTransactionsAvailable\":true,\"numberOfTransactions\":3,\"numberOfReservedTransactions\":2,\"numberOfBankGiroPrognosisTransactions\":1,\"bankGiroPrognosisTransactions\":[{\"categoryId\":0,\"date\":\"2020-09-23\",\"amount\":\"10 000,00\",\"description\":\"Ingår ej i tillgängligt belopp\",\"currency\":\"SEK\",\"bgprognosTypeDescription\":\"Bg prognos.\",\"bgprognosType\":\"DEFAULT\"}],\"links\":{\"next\":{\"method\":\"GET\",\"uri\":\"/v5/engagement/transactions/de525608fd7c11eaadc10242ac1200020000002b?page=next&paginationId=de525608fd7c11eaadc10242ac1200020000002b\"}}}\n",
                        EngagementTransactionsResponse.class);

        List<Transaction> result = engagementTransactionsResponse.toTransactions();

        assertEquals(3, result.size());
    }
}
