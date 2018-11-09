package se.tink.backend.aggregation.nxgen.agents.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class NextGenerationTransactionGenerator {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final List<GenerationBase> generationBase;
    private final Random randomGenerator;
    private static String generationBaseFile = "generationbase.json";
    private LocalDate date;

    int numOfTransactions = 1000;

    public NextGenerationTransactionGenerator(String basePath) throws IOException {
        generationBase = loadGenerationBase(basePath + "/" + generationBaseFile);
        this.randomGenerator = new Random();
        setTodaysDate();
    }

    //Generate todays date for iteration
    private void setTodaysDate() {
        Date input = new Date();
        Instant instant = input.toInstant();
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        this.date = zdt.toLocalDate();
    }

    private List<GenerationBase> loadGenerationBase(String path) throws IOException {
        File generationConfig = new File(path);
        if (generationConfig == null) {
            throw new IOException("no provider file found");
        }

        List<GenerationBase> generationBases =
                mapper.readValue(generationConfig, new TypeReference<List<GenerationBase>>(){});
        return generationBases;
    }

    //Randomises a purchase up to between 1-5 items
    private double randomisePurchase(GenerationBase base) {
        double finalPrice = 0;
        for (Double price : base.getItemPrices()) {
            finalPrice += (randomGenerator.nextInt(4) + 1) * price;
        }

        finalPrice *= base.getCurrencyMultiplier();
        return finalPrice;
    }

    //Average out to ~5 transactions per day
    private void randomDateDecrement() {
        if(randomGenerator.nextInt(5) < 1) {
            date = date.minusDays(1);
        }
    }

    private Transaction generateTransaction(GenerationBase base) {
        double finalPrice = randomisePurchase(base);
        randomDateDecrement();

        return Transaction.builder()
                .setPending(false)
                .setDescription(base.getCompany())
                .setAmount(new Amount(base.getCurrency(), finalPrice))
                .setDate(date)
                .build();
    }

    public Collection<Transaction> generateTransactions() {
        Collection<Transaction> transactions = Collections.EMPTY_LIST;
        for (int i = 0; 0 < numOfTransactions; i++) {
            GenerationBase base = generationBase.get(randomGenerator.nextInt(generationBase.size()));
            transactions.add(generateTransaction(base));
        }

        return transactions;
    }


    private static class GenerationBase {
        private String company;
        private List<Double> itemPrices;
        private String currency;
        private double currencyMultiplier;

        public String getCompany() {
            return company;
        }

        public List<Double> getItemPrices() {
            return itemPrices;
        }

        public String getCurrency() {
            return currency;
        }

        public double getCurrencyMultiplier() {
            return currencyMultiplier;
        }

        public GenerationBase(String company, List<Double> itemPrices, String currency, double currencyMultiplier) {
            this.company = company;
            this.itemPrices = itemPrices;
            this.currency = currency;
            this.currencyMultiplier = currencyMultiplier;
        }
    }
}
