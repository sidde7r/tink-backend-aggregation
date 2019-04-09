package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ArgentaTransaction {

    String id;
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    Date transactionDate;

    Double amount;
    String currency;
    String iban;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    Date currencyDate;

    String beneficiaryIban;
    String beneficiaryName;
    List<String> messageLines;
    String reference;
    String transferType;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDescription(getDescription())
                .setDate(getDate())
                .setExternalId(reference)
                .build();
    }

    private String getDescription() {
        String tinkDescription = description;
        if (!Strings.isNullOrEmpty(beneficiaryName)) {
            tinkDescription = tinkDescription.concat(" " + beneficiaryName);
        }
        if (messageLines != null && messageLines.size() > 0) {
            if (!messageLines.get(0).equalsIgnoreCase(beneficiaryName)) {
                tinkDescription = tinkDescription.concat(" " + messageLines.get(0));
            }
        }
        return tinkDescription;
    }

    private Amount getAmount() {
        if (currency != null) return new Amount(currency, amount);
        return Amount.inEUR(amount);
    }

    private Date getDate() {
        return transactionDate;
    }
}
