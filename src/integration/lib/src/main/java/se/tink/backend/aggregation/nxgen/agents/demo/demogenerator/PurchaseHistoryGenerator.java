package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import static java.util.stream.Collectors.toList;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

public class PurchaseHistoryGenerator {

    private final List<GeneratePurchaseBase> generatePurchaseBase;
    private final Random randomGenerator;
    private final DemoFileHandler demoFileHandler;
    private static final int AVEREAGE_PURCHASES_PER_DAY = 3;

    // TODO: Should be persisted between refreshes. Store on disk is not an alternative
    public PurchaseHistoryGenerator(String basePath) {
        this.demoFileHandler = new DemoFileHandler(basePath);
        generatePurchaseBase = this.demoFileHandler.getGeneratePurchaseBase();
        this.randomGenerator = new SecureRandom();
    }

    private double randomisePurchase(GeneratePurchaseBase base, String currency) {
        double finalPrice = 0;
        for (Double price : base.getItemPrices()) {
            finalPrice += (randomGenerator.nextInt(AVEREAGE_PURCHASES_PER_DAY) + 1) * -price;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.parseDouble(
                decimalFormat.format(
                        DemoConstants.getSekToCurrencyConverter(currency, finalPrice)));
    }

    private Transaction generateTransaction(
            GeneratePurchaseBase base, LocalDate dateCursor, String currency) {
        double finalPrice = randomisePurchase(base, currency);

        return generateTransaction(
                finalPrice, base.getCompany(), dateCursor, currency, TransactionTypes.TRANSFER);
    }

    private Transaction generateTransaction(
            double finalPrice,
            String description,
            LocalDate dateCursor,
            String currency,
            TransactionTypes transactionType) {

        return Transaction.builder()
                .setPending(false)
                .setDescription(description)
                .setAmount(ExactCurrencyAmount.of(finalPrice, currency))
                .setDate(dateCursor)
                .setType(transactionType)
                .build();
    }

    private Collection<Transaction> generateOneDayOfTransactions(
            LocalDate dateCursor, String currency) {
        List<Transaction> transactions = new ArrayList<>();
        // Between one and 4 purchases per day.
        for (int i = 0; i < randomGenerator.nextInt(3) + 1; i++) {
            GeneratePurchaseBase base =
                    generatePurchaseBase.get(randomGenerator.nextInt(generatePurchaseBase.size()));
            transactions.add(generateTransaction(base, dateCursor, currency));
        }

        return transactions;
    }

    private Transaction generateSingleTransaction(
            String amount,
            String description,
            LocalDate dateCursor,
            String currency,
            TransactionTypes transactionType) {

        return generateTransaction(
                Double.parseDouble(amount), description, dateCursor, currency, transactionType);
    }

    public PaginatorResponse generateTransactions(
            Date from, Date to, String currency, int maxNumberOfDailyTransactions) {
        LocalDate start = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Transaction> transactions = new ArrayList<>();

        if (Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() == 0
                || Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() == 1) {
            transactions.addAll(generateOneDayOfTransactions(start, currency));
            return PaginatorResponseImpl.create(transactions, false);
        }

        for (LocalDate dateCursor = start;
                dateCursor.isBefore(end);
                dateCursor = dateCursor.plusDays(1)) {
            transactions.addAll(generateOneDayOfTransactions(dateCursor, currency));
            if (transactions.size() >= maxNumberOfDailyTransactions) {
                break;
            }
        }

        transactions.addAll(addMontlyRecurringCost(from, to, "Salary", 3500));
        transactions.addAll(addMontlyRecurringCost(from, to, "Netflix", -9.99));
        transactions.addAll(addMontlyRecurringCost(from, to, "Spotify", -9.99));
        transactions.addAll(addMontlyRecurringCost(from, to, "Gym Membership", -45.99));

        // Adding transactions with different types
        transactions.add(
                generateSingleTransaction(
                        "-50", "Withdrawal", end, currency, TransactionTypes.WITHDRAWAL));
        transactions.add(
                generateSingleTransaction("-5", "Fee", end, currency, TransactionTypes.DEFAULT));
        transactions.add(
                generateSingleTransaction(
                        "-25.60", "Card operation", end, currency, TransactionTypes.CREDIT_CARD));

        return PaginatorResponseImpl.create(transactions, false);
    }

    // TODO: Add nicer logic for generation of savings. Make sure to add up to the sum of the
    // account
    public PaginatorResponse generateSavingsAccountTransactions(
            TransactionalAccount account, Date from, Date to) {
        int numberOfMonths = (int) DateUtils.getNumberOfMonthsBetween(from, to);
        List<Transaction> transactions =
                IntStream.range(0, numberOfMonths)
                        .mapToObj(
                                i ->
                                        Transaction.builder()
                                                .setAmount(
                                                        ExactCurrencyAmount.of(
                                                                account.getExactBalance()
                                                                                .getDoubleValue()
                                                                        / 36,
                                                                account.getExactBalance()
                                                                        .getCurrencyCode()))
                                                .setPending(false)
                                                .setDescription("monthly savings")
                                                .setType(TransactionTypes.TRANSFER)
                                                .setDate(
                                                        DateUtils.addMonths(
                                                                DateUtils.getToday(), -1))
                                                .build())
                        .collect(toList());

        return PaginatorResponseImpl.create(transactions, false);
    }

    private List<Transaction> addMontlyRecurringCost(
            Date from, Date to, String name, double amount) {
        TransactionTypes type = (amount < 0) ? TransactionTypes.PAYMENT : TransactionTypes.TRANSFER;
        String currency = "EUR";
        int numberOfMonths = (int) DateUtils.getNumberOfMonthsBetween(from, to);
        List<Transaction> transactions =
                IntStream.range(0, numberOfMonths)
                        .mapToObj(
                                i ->
                                        Transaction.builder()
                                                .setAmount(ExactCurrencyAmount.of(amount, currency))
                                                .setPending(false)
                                                .setDescription(name)
                                                .setType(type)
                                                .setDate(
                                                        DateUtils.addMonths(
                                                                DateUtils.getToday(), -i))
                                                .build())
                        .collect(toList());

        return transactions;
    }
}
