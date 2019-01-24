package se.tink.backend.aggregation.agents.creditcards.sebkort;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.CardGroupEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.ContractEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceBillingUnitEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.SummaryTransactionsEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.TransactionEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.TransactionGroupEntity;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SEBKortParserTest {

    private SEBKortParser parser;
    private Integer transactionCounter = 0;

    @Before
    public void setup() {
        parser = new SEBKortParser();
    }

    @Test
    public void testDeduplicatesTransactions() throws ParseException {
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCardGroupForAccount("345678******3456");
        List<TransactionEntity> transactions = getTransactionsForFirstAccount(invoiceDetails);

        TransactionEntity transaction;
        transaction = buildTransaction();
        transaction.setTransactionId("550");
        transactions.add(transaction);

        transaction = buildTransaction();
        transaction.setTransactionId("550");
        transactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("345678******3456");
        assertNotNull(accountTransactions);
        assertEquals(1, accountTransactions.size());
    }

    @Test
    public void testAccountLessTransactionsArePutIntoAnAccount() throws ParseException {
        TransactionEntity transaction;

        // Add a transaction that doesn't belong to an account, an invoice payment for instance
        InvoiceDetailsEntity emptyInvoiceDetails = buildInvoiceDetails();
        List<TransactionEntity> summaryTransactions = getSummaryTransactions(emptyInvoiceDetails);
        transaction = buildTransaction();
        transaction.setOriginalAmountDate("08-30");
        transaction.setDescription("BETALT BG DATUM 130830");
        summaryTransactions.add(transaction);

        parser.parseInvoiceDetails(emptyInvoiceDetails);

        // Add a "normal" transaction
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCardGroupForAccount("123456******1234");
        List<TransactionEntity> transactions = getTransactionsForFirstAccount(invoiceDetails);
        transaction = buildTransaction();
        transaction.setOriginalAmountDate("07-20");
        transaction.setDescription("ICA SUPERMARKET WALLIN");
        transactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("123456******1234");
        assertNotNull(accountTransactions);
        assertEquals(2, accountTransactions.size());
    }

    @Test
    public void testPendingAccountLessTransactionsArePutIntoAnAccountOnlyOnce() throws ParseException {
        TransactionEntity transaction;

        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCardGroupForAccount("123456******1234");

        // Add a normal transaction
        List<TransactionEntity> transactions = getTransactionsForFirstAccount(invoiceDetails);
        transaction = buildTransaction();
        transaction.setDescription("ICA SUPERMARKET WALLIN");
        transactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        // Add a transaction that doesn't belong to an account, an invoice payment for instance
        ContractEntity contractEntity = new ContractEntity();

        InvoiceDetailsEntity pendingInvoiceDetails = buildInvoiceDetails();

        List<TransactionEntity> summaryTransactions = getPendingSummaryTransactions(pendingInvoiceDetails);
        transaction = buildTransaction();
        transaction.setDescription("BETALT BG DATUM 130830");
        summaryTransactions.add(transaction);

        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails);
        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("123456******1234");
        assertNotNull(accountTransactions);
        assertEquals(2, accountTransactions.size());
    }

    @Test
    public void testHandlesTransactionsWithoutIds() throws ParseException {
        TransactionEntity transaction;
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCardGroupForAccount("123456******1234");

        List<TransactionEntity> transactions = getTransactionsForFirstAccount(invoiceDetails);
        transaction = buildTransaction();
        transaction.setTransactionId(null);
        transaction.setDescription("KREDITRÄNTA");
        transactions.add(transaction);

        transaction = buildTransaction();
        transaction.setTransactionId(null);
        transaction.setDescription("BETALT BG DATUM 130830");
        transactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("123456******1234");
        assertNotNull(accountTransactions);
        assertEquals(2, accountTransactions.size());
    }

    private List<TransactionEntity> getPendingSummaryTransactions(InvoiceDetailsEntity invoiceDetails) {
        if (invoiceDetails.getTransactionGroups() == null) {
            List<TransactionGroupEntity> transactionGroups = Lists.newArrayList();
            invoiceDetails.setTransactionGroups(transactionGroups);
            TransactionGroupEntity transactionGroup = new TransactionGroupEntity();
            transactionGroups.add(transactionGroup);
            List<TransactionEntity> transacitons = Lists.newArrayList();
            transactionGroup.setTransactions(transacitons);
        }
        return invoiceDetails.getTransactionGroups().get(0).getTransactions();
    }

    @Test
    public void testAccountLessTransactionsThatAreJustSummariesAreDiscarded() throws ParseException {
        TransactionEntity transaction;

        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCardGroupForAccount("234567******2345");

        List<TransactionEntity> summaryTransactions = getSummaryTransactions(invoiceDetails);

        transaction = buildTransaction();
        transaction.setDescription("Saldo fr√•n f√∂reg√•ende m√•nad"); // It really looks like this in the API
        summaryTransactions.add(transaction);

        transaction = buildTransaction();
        transaction.setDescription("Summa k√∂p/uttag"); // Yes, actual API data
        summaryTransactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("234567******2345");
        assertNotNull(accountTransactions);
        assertEquals(0, accountTransactions.size());
    }

    @Test
    public void testAccountBalanceFromCobrandCardGroups() throws ParseException {
        InvoiceBillingUnitEntity billingUnit = new InvoiceBillingUnitEntity();

        // ReservedAmount = 151
        // Credit from last invoice = 2.69
        billingUnit.setBalance("16 579,23 kr");
        billingUnit.setUnInvoicedAmount("6 921,61 kr");
        parser.parseBillingUnit(billingUnit);

        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetails();

        addCardGroupForAccount(invoiceDetails, "123456******1234", 3366.02);
        addCobrandCardGroupForAccount(invoiceDetails, "123456******1234", 1891.62);
        addCardGroupForAccount(invoiceDetails, "234567******2345", 3984.68);
        addCobrandCardGroupForAccount(invoiceDetails, "234567******2345", 417.99);

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setUnInvoicedAmount("6 770,61 kr");

        InvoiceDetailsEntity pendingInvoiceDetails = buildInvoiceDetails();

        addCardGroupForAccount(pendingInvoiceDetails, "123456******1234", 3177.77);
        addCobrandCardGroupForAccount(pendingInvoiceDetails, "123456******1234", 896.35);
        addCardGroupForAccount(pendingInvoiceDetails, "234567******2345", 2696.49);
        addCobrandCardGroupForAccount(pendingInvoiceDetails, "234567******2345", 0);

        parser.parseInvoiceDetails(invoiceDetails);
        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails);

        Map<String, Account> accounts = parser.getAccountsByAccountNumber();
        double balance1 = accounts.get("123456******1234").getBalance();
        double balance2 = accounts.get("234567******2345").getBalance();
        assertEquals(-9480.07, balance1, .001);
        assertEquals(-7099.16, balance2, .001);
    }

    @Test
    public void testDoesntCountLatestInvoiceIfPaid() throws Exception {
        InvoiceBillingUnitEntity billingUnit = new InvoiceBillingUnitEntity();

        billingUnit.setBalance("6 000,00 kr");
        billingUnit.setUnInvoicedAmount("6 000,00 kr");
        parser.parseBillingUnit(billingUnit);

        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetails();
        addCardGroupForAccount(invoiceDetails, "123456******1234", 4000);
        addCardGroupForAccount(invoiceDetails, "234567******2345", 4000);

        ContractEntity contractEntity = new ContractEntity();

        InvoiceDetailsEntity pendingInvoiceDetails = buildInvoiceDetails();
        addCardGroupForAccount(pendingInvoiceDetails, "123456******1234", 3000);
        addCardGroupForAccount(pendingInvoiceDetails, "234567******2345", 3000);

        parser.parseInvoiceDetails(invoiceDetails);
        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails);

        Map<String, Account> accounts = parser.getAccountsByAccountNumber();
        double balance1 = accounts.get("123456******1234").getBalance();
        double balance2 = accounts.get("234567******2345").getBalance();
        assertEquals(-3000, balance1, .001);
        assertEquals(-3000, balance2, .001);
    }

    @Test
    public void testOnlyUsesOnePendingInvoiceDetails() throws Exception {
        ContractEntity contractEntity = new ContractEntity();

        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetails();
        addCardGroupForAccount(invoiceDetails, "123456******1234", 0);

        InvoiceDetailsEntity pendingInvoiceDetails1 = buildInvoiceDetails();
        addCardGroupForAccount(pendingInvoiceDetails1, "123456******1234", 5000);

        InvoiceDetailsEntity pendingInvoiceDetails2 = buildInvoiceDetails();
        addCardGroupForAccount(pendingInvoiceDetails2, "123456******1234", 3000);

        parser.parseInvoiceDetails(invoiceDetails);
        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails1);
        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails2);

        Map<String, Account> accounts = parser.getAccountsByAccountNumber();
        double balance1 = accounts.get("123456******1234").getBalance();
        assertEquals(-3000, balance1, .001);
    }

    @Test
    public void testCobrandCardTransactionsAreParsed() throws ParseException {
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCobrandCardGroupForAccount("345678******3456");
        List<TransactionEntity> transactions = getTransactionsForFirstCobrandAccount(invoiceDetails);

        TransactionEntity transaction;
        transaction = buildTransaction();
        transactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("345678******3456");
        assertNotNull(accountTransactions);
        assertEquals(1, accountTransactions.size());
    }

    @Test
    public void testTransactionsFromCobrand() throws ParseException {
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetailsWithOneCobrandCardGroupForAccount("345678******3456");
        List<TransactionEntity> transactions = getTransactionsForFirstCobrandAccount(invoiceDetails);

        TransactionEntity transaction;
        transaction = buildTransaction();
        transactions.add(transaction);

        parser.parseInvoiceDetails(invoiceDetails);

        List<Transaction> accountTransactions = parser.getTransactionsByAccountNumber().get("345678******3456");
        assertNotNull(accountTransactions);
        assertEquals(1, accountTransactions.size());
    }

    @Test
    public void differentArrangementNumbers_doNotShareInvoiceDetails() throws ParseException {
        InvoiceBillingUnitEntity billingUnit1 = new InvoiceBillingUnitEntity();
        InvoiceBillingUnitEntity billingUnit2 = new InvoiceBillingUnitEntity();

        // ReservedAmount = 151
        // Credit from last invoice = 2.69
        billingUnit1.setBalance("16 579,23 kr");
        billingUnit1.setUnInvoicedAmount("6 921,61 kr");
        parser.parseBillingUnit(billingUnit1);

        billingUnit2.setBalance("16 579,23 kr");
        billingUnit2.setUnInvoicedAmount("6 921,61 kr");
        parser.parseBillingUnit(billingUnit2);

        InvoiceDetailsEntity invoiceDetails1 = buildInvoiceDetailsWithArrangementNumber("12345");
        InvoiceDetailsEntity invoiceDetails2 = buildInvoiceDetailsWithArrangementNumber("23456");

        addCardGroupForAccount(invoiceDetails1, "123456******1234", 3366.02);
        addCobrandCardGroupForAccount(invoiceDetails1, "123456******1234", 1891.62);
        addCardGroupForAccount(invoiceDetails1, "234567******2345", 3984.68);
        addCobrandCardGroupForAccount(invoiceDetails1, "234567******2345", 417.99);

        addCardGroupForAccount(invoiceDetails2, "234567******1234", 3366.02);
        addCobrandCardGroupForAccount(invoiceDetails2, "234567******1234", 1891.62);
        addCardGroupForAccount(invoiceDetails2, "123456******2345", 3984.68);
        addCobrandCardGroupForAccount(invoiceDetails2, "123456******2345", 417.99);

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setUnInvoicedAmount("6 770,61 kr");

        InvoiceDetailsEntity pendingInvoiceDetails = buildInvoiceDetails();

        addCardGroupForAccount(pendingInvoiceDetails, "123456******1234", 3177.77);
        addCobrandCardGroupForAccount(pendingInvoiceDetails, "123456******1234", 896.35);
        addCardGroupForAccount(pendingInvoiceDetails, "234567******2345", 2696.49);
        addCobrandCardGroupForAccount(pendingInvoiceDetails, "234567******2345", 0);

        parser.parseInvoiceDetails(invoiceDetails1);
        parser.parseInvoiceDetails(invoiceDetails2);
        parser.parsePendingInvoiceDetails(contractEntity, pendingInvoiceDetails);

        Map<String, Account> accounts = parser.getAccountsByAccountNumber();
        double balance1 = accounts.get("123456******1234").getBalance();
        double balance2 = accounts.get("234567******2345").getBalance();
        double balance3 = accounts.get("234567******1234").getBalance();
        double balance4 = accounts.get("123456******2345").getBalance();

        assertEquals(-9480.07, balance1, .001);
        assertEquals(-7099.16, balance2, .001);
        assertEquals(-12176.56, balance3, .001);
        assertEquals(-4402.67, balance4, .001);
    }

    protected TransactionEntity buildTransaction() {
        TransactionEntity transaction;
        transaction = new TransactionEntity();
        transaction.setTransactionId((transactionCounter++).toString());
        transaction.setDescription("");
        return transaction;
    }

    protected List<TransactionEntity> getSummaryTransactions(InvoiceDetailsEntity invoiceDetails) {
        return invoiceDetails.getSummaryTransactions().getTransactions();
    }

    protected List<TransactionEntity> getTransactionsForFirstCobrandAccount(InvoiceDetailsEntity invoiceDetails) {
        return invoiceDetails.getCobrandCardGroups().get(0).getTransactionGroups().get(0).getTransactions();
    }

    protected List<TransactionEntity> getTransactionsForFirstAccount(InvoiceDetailsEntity invoiceDetails) {
        return invoiceDetails.getCardGroups().get(0).getTransactionGroups().get(0).getTransactions();
    }

    protected InvoiceDetailsEntity buildInvoiceDetailsWithOneCobrandCardGroupForAccount(String accountNumber) {
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetails();
        CardGroupEntity cardGroup = buildCardGroupForAccount(accountNumber);
        invoiceDetails.getCobrandCardGroups().add(cardGroup);
        return invoiceDetails;
    }

    protected InvoiceDetailsEntity buildInvoiceDetailsWithOneCardGroupForAccount(String accountNumber) {
        InvoiceDetailsEntity invoiceDetails = buildInvoiceDetails();
        CardGroupEntity cardGroup = buildCardGroupForAccount(accountNumber);
        invoiceDetails.getCardGroups().add(cardGroup);
        return invoiceDetails;
    }

    protected InvoiceDetailsEntity buildInvoiceDetails() {
        InvoiceDetailsEntity invoiceDetails = new InvoiceDetailsEntity();
        SummaryTransactionsEntity summaryTransactions = new SummaryTransactionsEntity();
        invoiceDetails.setSummaryTransactions(summaryTransactions);
        summaryTransactions.setTransactions(new ArrayList<TransactionEntity>());
        List<CardGroupEntity> cardGroups = new ArrayList<>();
        invoiceDetails.setCardGroups(cardGroups);
        List<CardGroupEntity> cobrandCardGroups = new ArrayList<>();
        invoiceDetails.setCobrandCardGroups(cobrandCardGroups);
        invoiceDetails.setInvoiceDate("2015-01-05");
        invoiceDetails.setArrangementNumber("12345");
        return invoiceDetails;
    }

    protected InvoiceDetailsEntity buildInvoiceDetailsWithArrangementNumber(String arrangementNumber) {
        InvoiceDetailsEntity invoiceDetails = new InvoiceDetailsEntity();
        SummaryTransactionsEntity summaryTransactions = new SummaryTransactionsEntity();
        invoiceDetails.setSummaryTransactions(summaryTransactions);
        summaryTransactions.setTransactions(new ArrayList<TransactionEntity>());
        List<CardGroupEntity> cardGroups = new ArrayList<>();
        invoiceDetails.setCardGroups(cardGroups);
        List<CardGroupEntity> cobrandCardGroups = new ArrayList<>();
        invoiceDetails.setCobrandCardGroups(cobrandCardGroups);
        invoiceDetails.setInvoiceDate("2015-01-05");
        invoiceDetails.setArrangementNumber(arrangementNumber);
        return invoiceDetails;
    }

    protected CardGroupEntity buildCardGroupForAccount(String accountNumber) {
        CardGroupEntity cardGroup = new CardGroupEntity();
        cardGroup.setMaskedCardNumber(accountNumber);
        List<TransactionGroupEntity> transactionGroups = new ArrayList<>();
        cardGroup.setTransactionGroups(transactionGroups);
        TransactionGroupEntity transactionGroup = new TransactionGroupEntity();
        transactionGroups.add(transactionGroup);
        transactionGroup.setTransactions(new ArrayList<TransactionEntity>());
        return cardGroup;
    }

    protected void addCobrandCardGroupForAccount(InvoiceDetailsEntity invoiceDetails, String accountNumber,
            double balance) {
        CardGroupEntity cobrandCardGroup = buildCardGroupForAccount(accountNumber);
        cobrandCardGroup.setTotalNumber(balance);
        invoiceDetails.getCardGroups().add(cobrandCardGroup);
    }

    protected void addCardGroupForAccount(InvoiceDetailsEntity invoiceDetails, String accountNumber, double balance) {
        CardGroupEntity cardGroup = buildCardGroupForAccount(accountNumber);
        cardGroup.setTotalNumber(balance);
        invoiceDetails.getCardGroups().add(cardGroup);
    }
}
