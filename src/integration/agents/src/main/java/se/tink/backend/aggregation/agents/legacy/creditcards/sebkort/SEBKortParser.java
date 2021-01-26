package se.tink.backend.aggregation.agents.creditcards.sebkort;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.CardGroupEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.ContractEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceBillingUnitEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.InvoiceDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.SummaryTransactionsEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.TransactionEntity;
import se.tink.backend.aggregation.agents.creditcards.sebkort.model.TransactionGroupEntity;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SEBKortParser {
    private static final Ordering<Transaction> TRANSACTION_ORDERING =
            new Ordering<Transaction>() {
                @Override
                public int compare(Transaction left, Transaction right) {
                    return ComparisonChain.start()
                            .compare(right.getDate(), left.getDate())
                            .compare(right.getDescription(), left.getDescription())
                            .compare(right.getId(), left.getId())
                            .result();
                }
            };

    private static class AccountDebt {
        public Date date;
        public String accountNumber;
        public double debt;
    }

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Double totalDebt;
    private Double unInvoicedDebt;
    private Set<String> transactionHashes = Sets.newHashSet();
    private Map<String, Account> accountsByAccountNumber = Maps.newHashMap();
    private Map<String, List<Transaction>> transactionsByAccountNumber = Maps.newHashMap();
    private Map<String, Double> pendingDebtByAccountNumber = Maps.newHashMap();
    private Map<String, String> primaryAccountByArrangementNumber = Maps.newHashMap();
    private Set<AccountDebt> accountDebts = Sets.newHashSet();
    private List<Transaction> defferedAccountlessTransactions = Lists.newArrayList();
    private Multimap<String, String> accountsByArrangementNumber = ArrayListMultimap.create();

    private Account getPrimaryAccountForArrangementNumber(String arrangementNumber) {
        String accountNumber = primaryAccountByArrangementNumber.get(arrangementNumber);
        return accountsByAccountNumber.get(accountNumber);
    }

    public Map<String, Account> getAccountsByAccountNumber() {
        return accountsByAccountNumber;
    }

    public Map<String, List<Transaction>> getTransactionsByAccountNumber() {
        return transactionsByAccountNumber;
    }

    public void parseBillingUnit(InvoiceBillingUnitEntity invoiceBillingUnit) {
        totalDebt = -parseSebAmount(invoiceBillingUnit.getBalance());
        unInvoicedDebt = -parseSebAmount(invoiceBillingUnit.getUnInvoicedAmount());
    }

    public void parseInvoiceDetails(InvoiceDetailsEntity invoiceDetails) throws ParseException {
        String arrangementNumber = invoiceDetails.getArrangementNumber();
        Date invoiceDate = parseInvoiceDate(invoiceDetails);

        // Loop through the card groups and cobrand card groups and add the accounts and
        // transactions.

        List<CardGroupEntity> cardGroups = invoiceDetails.getCardGroups();
        cardGroups.addAll(invoiceDetails.getCobrandCardGroups());

        for (CardGroupEntity cardGroup : cardGroups) {
            String accountName = invoiceDetails.getProductName();
            String accountNumber = cardGroup.getMaskedCardNumber();

            Account account = findOrAddAccount(accountNumber, accountName);

            AccountDebt debt = new AccountDebt();
            debt.debt = -cardGroup.getTotalNumber();
            debt.accountNumber = cardGroup.getMaskedCardNumber();
            debt.date = invoiceDate;
            accountDebts.add(debt);

            if (!primaryAccountByArrangementNumber.containsKey(arrangementNumber)) {
                primaryAccountByArrangementNumber.put(arrangementNumber, accountNumber);
            }

            if (!accountsByArrangementNumber.get(arrangementNumber).contains(accountNumber)) {
                accountsByArrangementNumber.put(arrangementNumber, accountNumber);
            }

            addTransactionsFromCardGroup(cardGroup, account, invoiceDate);
        }

        SummaryTransactionsEntity summaryTransactions = invoiceDetails.getSummaryTransactions();
        if (summaryTransactions != null) {
            addSummaryTransactionsToPrimaryAccount(
                    summaryTransactions.getTransactions(), invoiceDate, arrangementNumber);
        }

        calculateAccountBalances(arrangementNumber);
    }

    public void parsePendingInvoiceDetails(
            ContractEntity contractEntity, InvoiceDetailsEntity invoiceDetails)
            throws ParseException {

        List<TransactionGroupEntity> transactionGroups = invoiceDetails.getTransactionGroups();
        if (transactionGroups != null && transactionGroups.size() > 0) {
            addSummaryTransactionsToPrimaryAccount(
                    transactionGroups.get(0).getTransactions(),
                    null,
                    invoiceDetails.getArrangementNumber());
        }

        // Loop through the card groups and cobrand card groups and add the accounts and
        // transactions.

        List<CardGroupEntity> cardGroups = invoiceDetails.getCardGroups();
        cardGroups.addAll(invoiceDetails.getCobrandCardGroups());

        Map<String, Double> pending = Maps.newHashMap();
        for (CardGroupEntity cardGroup : cardGroups) {
            String accountNumber = cardGroup.getMaskedCardNumber();
            String accountName = contractEntity.getContractName();
            Account account = findOrAddAccount(accountNumber, accountName);

            Double balance = pending.get(accountNumber);
            if (balance == null) {
                balance = 0.0;
            }
            pending.put(accountNumber, balance - cardGroup.getTotalNumber());

            addTransactionsFromCardGroup(cardGroup, account, null);
        }
        pendingDebtByAccountNumber.putAll(pending);

        calculateAccountBalances(invoiceDetails.getArrangementNumber());
    }

    private void calculateAccountBalances(String arrangementNumber) {
        List<Account> accounts = Lists.newArrayList();
        for (String accountNumber : accountsByArrangementNumber.get(arrangementNumber)) {
            accounts.add(accountsByAccountNumber.get(accountNumber));
        }

        if (accounts.isEmpty()) {
            return;
        }
        double accountsDebt = 0;
        for (Account account : accounts) {
            double b = balanceForAccount(account.getAccountNumber());
            account.setBalance(b);
            accountsDebt += b;
        }
        if (totalDebt == null) {
            return;
        }
        double diff = totalDebt - accountsDebt;
        Account primaryAccount = getPrimaryAccountForArrangementNumber(arrangementNumber);
        primaryAccount.setBalance(primaryAccount.getBalance() + diff);
    }

    private double balanceForAccount(String accountNumber) {
        double accountDebt = 0;
        accountDebt += getPendingAmountForAccount(accountNumber);

        if (!isAllInvoicesPaid()) {
            List<AccountDebt> debts = Lists.newArrayList();
            Date maxDate = null;

            for (AccountDebt debt : accountDebts) {
                if (debt.accountNumber.equals(accountNumber)) {
                    debts.add(debt);
                }

                if (maxDate == null || debt.date.after(maxDate)) {
                    maxDate = debt.date;
                }
            }

            for (AccountDebt debt : debts) {
                if (debt.date.equals(maxDate)) {
                    accountDebt += debt.debt;
                }
            }
        }
        return accountDebt;
    }

    private double getPendingAmountForAccount(String accountNumber) {
        Double pending = pendingDebtByAccountNumber.get(accountNumber);
        if (pending == null) {
            pending = 0.0;
        }
        return pending;
    }

    private List<Transaction> getTransactionsForAccount(Account account) {
        String accountNumber = account.getAccountNumber();
        List<Transaction> transactions = transactionsByAccountNumber.get(accountNumber);
        if (transactions == null) {
            transactions = Lists.newArrayList();
            transactionsByAccountNumber.put(accountNumber, transactions);
        }
        return transactions;
    }

    private Account findOrAddAccount(String accountNumber, String accountName) {
        Account account = accountsByAccountNumber.get(accountNumber);

        if (account == null) {
            account = new Account();
            account.setAccountNumber(accountNumber);
            account.setBankId(accountNumber);
            account.setType(AccountTypes.CREDIT_CARD);

            Preconditions.checkState(
                    Preconditions.checkNotNull(account.getBankId())
                            .matches("[0-9]{6}\\*{6}[0-9]{4}"),
                    "Unexpected account.bankid '%s'. Reformatted?",
                    account.getBankId());

            // Always set the name here as we only get the product name from
            // actual invoices, and the fake pending transactions invoice.

            account.setName(accountName);

            accountsByAccountNumber.put(accountNumber, account);
        }
        return account;
    }

    private boolean isAllInvoicesPaid() {
        if (totalDebt == null || unInvoicedDebt == null) {
            return false;
        }
        return totalDebt >= unInvoicedDebt;
    }

    private List<Transaction> addSummaryTransactionsToPrimaryAccount(
            List<TransactionEntity> transactions, Date invoiceDate, String arrangementNumber)
            throws ParseException {
        List<Transaction> accountlessTransactions = Lists.newArrayList();

        if (transactions != null) {
            for (TransactionEntity transactionEntity : transactions) {
                String description = transactionEntity.getDescription();

                // Skip the current period transaction sum and the incoming
                // account balance.
                if (description.startsWith("Summa ") || description.startsWith("Saldo ")) {
                    continue;
                }

                if (description.contains("KREDIT")) {
                    logger.info(description);
                }
                Transaction transaction = parseTransaction(transactionEntity, invoiceDate);
                if (checkAndMemorizeNewTransaction(transactionEntity, transaction)) {
                    accountlessTransactions.add(transaction);
                }
            }
        }

        Account primaryAccount = getPrimaryAccountForArrangementNumber(arrangementNumber);
        if (primaryAccount != null) {
            if (defferedAccountlessTransactions.size() > 0) {
                addTransactionsToAccount(primaryAccount, defferedAccountlessTransactions);
                defferedAccountlessTransactions.clear();
            }
            addTransactionsToAccount(primaryAccount, accountlessTransactions);
        } else {
            defferedAccountlessTransactions.addAll(accountlessTransactions);
        }

        return accountlessTransactions;
    }

    private void addTransactionsFromCardGroup(
            CardGroupEntity cardGroup, Account account, Date invoiceDate) throws ParseException {
        List<Transaction> transactions = Lists.newArrayList();
        for (TransactionGroupEntity transactionGroup : cardGroup.getTransactionGroups()) {

            for (TransactionEntity transactionEntity : transactionGroup.getTransactions()) {
                Transaction transaction = parseCardTransaction(transactionEntity, invoiceDate);

                if (checkAndMemorizeNewTransaction(transactionEntity, transaction)) {
                    transactions.add(transaction);
                }
            }
        }
        addTransactionsToAccount(account, transactions);
    }

    private void addTransactionsToAccount(Account account, List<Transaction> transactions) {
        List<Transaction> accountTransactions = getTransactionsForAccount(account);
        accountTransactions.addAll(transactions);
        Collections.sort(accountTransactions, TRANSACTION_ORDERING);
        updateStatus(CredentialsStatus.UPDATING, account, accountTransactions);
    }

    private Transaction parseCardTransaction(TransactionEntity transactionEntity, Date invoiceDate)
            throws ParseException {
        Transaction transaction = parseTransaction(transactionEntity, invoiceDate);

        if (transaction.getAmount() < 0) {
            transaction.setType(TransactionTypes.CREDIT_CARD);
        }

        return transaction;
    }

    private Transaction parseTransaction(TransactionEntity transactionEntity, Date invoiceDate)
            throws ParseException {
        Transaction transaction = null;
        transaction = new Transaction();

        String description = translateDescription(transactionEntity.getDescription());
        transaction.setDescription(description);
        transaction.setAmount(-transactionEntity.getAmountNumber());
        transaction.setDate(
                parseDateWithInvoiceDate(transactionEntity.getOriginalAmountDate(), invoiceDate));

        return transaction;
    }

    private boolean checkAndMemorizeNewTransaction(
            TransactionEntity transactionEntity, Transaction transaction) {
        String id = transactionEntity.getTransactionId();
        if (id == null) {
            id =
                    transaction.getDescription()
                            + "/"
                            + transaction.getAmount()
                            + "/"
                            + transaction.getDate().toString();
        }
        boolean isNew = !transactionHashes.contains(id);

        if (isNew) {
            transactionHashes.add(id);
        }

        return isNew;
    }

    private Date parseInvoiceDate(InvoiceDetailsEntity invoice) throws ParseException {
        String date = invoice.getInvoiceDate();

        // Backup date stamps if not invoice date is present
        if (Strings.isNullOrEmpty(date)) {
            date = invoice.getDueDate();
        }
        if (Strings.isNullOrEmpty(date)) {
            date = invoice.getBalanceDate();
        }
        if (Strings.isNullOrEmpty(date)) {
            date = invoice.getBalanceDateIn();
        }

        return DateUtils.flattenTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(date));
    }

    private double parseSebAmount(String amount) {
        return AgentParsingUtils.parseAmount(
                CharMatcher.whitespace().removeFrom(amount.replace("kr", "")));
    }

    /**
     * Construct a date from a MM-DD string format and an invoice date as reference in order to
     * determine the relevant year.
     *
     * @param text
     * @param invoiceDate
     * @return
     * @throws ParseException
     */
    private static Date parseDateWithInvoiceDate(String text, Date invoiceDate)
            throws ParseException {
        if (invoiceDate == null) {
            invoiceDate = DateUtils.getToday();
        }

        if (Strings.isNullOrEmpty(text)) {
            return invoiceDate;
        }

        Date date =
                ThreadSafeDateFormat.FORMATTER_DAILY.parse(
                        ThreadSafeDateFormat.FORMATTER_YEARLY.format(invoiceDate) + "-" + text);

        if (date.after(invoiceDate)) {
            Calendar calendar = DateUtils.getCalendar();

            calendar.setTime(date);
            calendar.add(Calendar.YEAR, -1);

            date = calendar.getTime();
        }

        if (date == null) {
            date = invoiceDate;
        }

        return DateUtils.flattenTime(date);
    }

    /**
     * Helper function to fix broken encoding on SEB Kort's part.
     *
     * @param text
     * @return
     */
    private static String translateDescription(String text) {
        return text.replace("{", "Ä")
                .replace("}", "Å")
                .replace("@", "Ö")
                .replace("$", "Å")
                .replace("#", "Ä");
    }

    private void updateStatus(
            CredentialsStatus updating, Account account, List<Transaction> transactions) {
        // Not updating this, since we currently are retrying SEB-kort fetching. Would
        // look weird for clients to see "3 accounts updated" jump to "1 account updated".
        /*if(context != null)
        statusUpdater.updateStatus(CredentialsStatus.UPDATING, account, transactions);*/
    }
}
