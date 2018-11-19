package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenDemoConstants;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class TransactionGenerator {

    private final List<GenerationBase> generationBase;
    private final Random randomGenerator;
    private final DemoFileHandler demoFileHandler;
    private final String currency;
    private final double currencyConvertionFactor;

    //TODO: Should be persisted between refreshes. Store on disk is not an alternative
    public TransactionGenerator(String basePath, String currency) throws IOException {
        this.demoFileHandler = new DemoFileHandler(basePath);
        generationBase = this.demoFileHandler.getGenerationBase();
        this.randomGenerator = new Random();
        this.currency = currency;
        this.currencyConvertionFactor = NextGenDemoConstants.getSekToCurrencyConverter(currency);
    }

    private double randomisePurchase(GenerationBase base) {
        double finalPrice = 0;
        for (Double price : base.getItemPrices()) {
            finalPrice += (randomGenerator.nextInt(2) + 1) * price;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.parseDouble(decimalFormat.format(finalPrice / currencyConvertionFactor));
    }

    private Transaction generateTransaction(GenerationBase base, LocalDate dateCursor) {
        double finalPrice = randomisePurchase(base);
        return Transaction.builder()
                .setPending(false)
                .setDescription(base.getCompany())
                .setAmount(new Amount(currency, finalPrice))
                .setDate(dateCursor)
                .build();
    }

    private Collection<Transaction> generateOneDayOfTransactions(LocalDate dateCursor) {
        ArrayList<Transaction> transactions = new ArrayList();
        //Between one and 4 purchases per day.
        for (int i = 0; i < randomGenerator.nextInt(3) + 1; i++) {
            GenerationBase base = generationBase.get(randomGenerator.nextInt(generationBase.size()));
            transactions.add(generateTransaction(base, dateCursor));
        }

        return transactions;
    }

    public Collection<Transaction> generateTransactions(Date from, Date to) {
        LocalDate start = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() == 0) {
            //TODO Generate two extra transactions for this date here
            return Collections.EMPTY_LIST;
        }

        ArrayList<Transaction> transactions = new ArrayList();
        for (LocalDate dateCursor = start; dateCursor.isBefore(end); dateCursor = dateCursor.plusDays(1)) {
            transactions.addAll(generateOneDayOfTransactions(dateCursor));
        }

        return transactions;
    }
}
