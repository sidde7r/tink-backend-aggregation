package se.tink.backend.aggregation.nxgen.agents.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
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
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class NextGenerationTransactionGenerator {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final List<GenerationBase> generationBase;
    private final Random randomGenerator;
    private static String generationBaseFile = NextGenDemoConstants.GENERATION_BASE_FILE;
    private LocalDate date;
    private final String currency;
    private final double currencyConvertionFactor;

    public NextGenerationTransactionGenerator(String basePath, String currency) throws IOException {
        generationBase = loadGenerationBase(basePath + File.separator + generationBaseFile);
        this.randomGenerator = new Random();
        setTodaysDate();
        this.currency = currency;
        this.currencyConvertionFactor = NextGenDemoConstants.getSekToCurrencyConverter(currency);
    }

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

    //Randomises a purchase up to between 1-3 items
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

        if (Duration.between(end.atStartOfDay(), date.atStartOfDay()).toDays() > 360) {
            return Collections.EMPTY_LIST;
        }

        ArrayList<Transaction> transactions = new ArrayList();
        for (LocalDate dateCursor = start; dateCursor.isBefore(end); dateCursor = dateCursor.plusDays(1)) {
            transactions.addAll(generateOneDayOfTransactions(dateCursor));
        }

        return transactions;
    }


    private static class GenerationBase {
        private String company;
        private List<Double> itemPrices;

        public String getCompany() {
            return company;
        }

        public List<Double> getItemPrices() {
            return itemPrices;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public void setItemPrices(List<Double> itemPrices) {
            this.itemPrices = itemPrices;
        }

        public GenerationBase() {
        }

    }
}
