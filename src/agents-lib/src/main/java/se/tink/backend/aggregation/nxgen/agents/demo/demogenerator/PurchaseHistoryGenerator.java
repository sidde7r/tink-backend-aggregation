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
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenDemoConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.DateUtils;
import static java.util.stream.Collectors.toList;

public class PurchaseHistoryGenerator {

    private final List<GenerationBase> generationBase;
    private final Random randomGenerator;
    private final DemoFileHandler demoFileHandler;

    //TODO: Should be persisted between refreshes. Store on disk is not an alternative
    public PurchaseHistoryGenerator(String basePath){
        this.demoFileHandler = new DemoFileHandler(basePath);
        generationBase = this.demoFileHandler.getGenerationBase();
        this.randomGenerator = new Random();
    }

    private double randomisePurchase(GenerationBase base, String currency) {
        double finalPrice = 0;
        for (Double price : base.getItemPrices()) {
            finalPrice += (randomGenerator.nextInt(2) + 1) * price;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.parseDouble(decimalFormat.format(finalPrice / NextGenDemoConstants.getSekToCurrencyConverter(currency)));
    }

    private Transaction generateTransaction(GenerationBase base, LocalDate dateCursor, String currency) {
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
            GenerationBase base = generationBase.get(randomGenerator.nextInt(generationBase.size()));
            transactions.add(generateTransaction(base, dateCursor, currency));
        }

        return transactions;
    }

    public PaginatorResponse generateTransactions(Date from, Date to, String currency) {
        LocalDate start = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (Duration.between(end.atStartOfDay(), start.atStartOfDay()).toDays() == 0) {
            //TODO Generate two extra transactions for this date here
            PaginatorResponseImpl.createEmpty(false);
        }

        ArrayList<Transaction> transactions = new ArrayList();
        for (LocalDate dateCursor = start; dateCursor.isBefore(end); dateCursor = dateCursor.plusDays(1)) {
            transactions.addAll(generateOneDayOfTransactions(dateCursor, currency));
        }

        return PaginatorResponseImpl.create(transactions, false);
    }

    //TODO: Add nicer logic for generation of savings. Make sure to add up to the sum of the account
    public PaginatorResponse createSavingsAccountTransactions(TransactionalAccount account) {
        List<Transaction> transactions = IntStream.range(0, 36)
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
