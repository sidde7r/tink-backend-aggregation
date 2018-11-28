package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

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
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.DateUtils;
import static java.util.stream.Collectors.toList;

public class PurchaseHistoryGenerator {

    private final List<GeneratePurchaseBase> generatePurchaseBase;
    private final Random randomGenerator;
    private final DemoFileHandler demoFileHandler;
    private static final int AVEREAGE_PURCHASES_PER_DAY = 3;

    //TODO: Should be persisted between refreshes. Store on disk is not an alternative
    public PurchaseHistoryGenerator(String basePath){
        this.demoFileHandler = new DemoFileHandler(basePath);
        generatePurchaseBase = this.demoFileHandler.getGeneratePurchaseBase();
        this.randomGenerator = new Random();
    }

    private double randomisePurchase(GeneratePurchaseBase base, String currency) {
        double finalPrice = 0;
        for (Double price : base.getItemPrices()) {
            finalPrice += (randomGenerator.nextInt(AVEREAGE_PURCHASES_PER_DAY) + 1) * price;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.parseDouble(decimalFormat.format(DemoConstants.getSekToCurrencyConverter(currency, finalPrice)));
    }

    private Transaction generateTransaction(GeneratePurchaseBase base, LocalDate dateCursor, String currency) {
        double finalPrice = randomisePurchase(base, currency);
        return Transaction.builder()
                .setPending(false)
                .setDescription(base.getCompany())
                .setAmount(new Amount(currency, finalPrice))
                .setDate(dateCursor)
                .build();
    }

    private Collection<Transaction> generateOneDayOfTransactions(LocalDate dateCursor, String currency) {
        ArrayList<Transaction> transactions = new ArrayList();
        //Between one and 4 purchases per day.
        for (int i = 0; i < randomGenerator.nextInt(3) + 1; i++) {
            GeneratePurchaseBase base = generatePurchaseBase.get(randomGenerator.nextInt(generatePurchaseBase.size()));
            transactions.add(generateTransaction(base, dateCursor, currency));
        }

        return transactions;
    }

    public PaginatorResponse generateTransactions(Date from, Date to, String currency) {
        LocalDate start = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        ArrayList<Transaction> transactions = new ArrayList();

        if (Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() == 0
                || Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() == 1) {
            transactions.addAll(generateOneDayOfTransactions(start, currency));
            return PaginatorResponseImpl.create(transactions, false);
        }


        for (LocalDate dateCursor = start; dateCursor.isBefore(end); dateCursor = dateCursor.plusDays(1)) {
            transactions.addAll(generateOneDayOfTransactions(dateCursor, currency));
        }

        return PaginatorResponseImpl.create(transactions, false);
    }

    //TODO: Add nicer logic for generation of savings. Make sure to add up to the sum of the account
    public PaginatorResponse generateSavingsAccountTransactions(TransactionalAccount account, Date from, Date to) {
        int numberOfMonths = (int) DateUtils.getNumberOfMonthsBetween(from, to);
        List<Transaction> transactions = IntStream.range(0, numberOfMonths)
                .mapToObj(i -> Transaction.builder()
                        .setAmount(new Amount(account.getBalance().getCurrency(),
                                account.getBalance().getValue() / 36))
                        .setPending(false)
                        .setDescription("monthly savings")
                        .setDate(DateUtils.addMonths(DateUtils.getToday(), -1)).build()
                )
                .collect(toList());

        return PaginatorResponseImpl.create(transactions, false);
    }

}
