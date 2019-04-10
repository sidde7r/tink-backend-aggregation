package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities;

import static com.google.common.base.Predicates.not;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsEntity {

    private String originIban;
    private Number amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String currencyCode;
    private String transactionCode;
    private String counterPartyName;
    private String paymentReference;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDate(bookingDate)
                .setDescription(getDescription())
                .setPending(false)
                .build();
    }

    private Amount getAmount() {
        return new Amount(currencyCode, amount);
    }

    private String getDescription() {
        return Stream.of(counterPartyName, paymentReference)
                .filter(not(Strings::isNullOrEmpty))
                .collect(Collectors.joining(" "));
    }
}
