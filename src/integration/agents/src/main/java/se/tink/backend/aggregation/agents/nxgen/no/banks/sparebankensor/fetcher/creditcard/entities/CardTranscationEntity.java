package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AmountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class CardTranscationEntity {
    private String accountNumber;
    private long accountingDate;

    @JsonProperty("accountingDate_iso8601")
    private Date accountDate;

    private AmountsEntity amounts;
    private String cardNumber;
    private String description;
    private String id;
    private MerchantEntity merchant;
    private String referenceNumber;
    private boolean reserved;
    private long transactionDateInLong;

    @JsonProperty("transactionDate_iso8601")
    private Date transactionDate;

    private String type;

    public CreditCardTransaction toTinkCardTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(ExactCurrencyAmount.of(amounts.getValue(), amounts.getCurrency()))
                .setPending(reserved)
                .setDescription(getDescription())
                .setDate(transactionDate)
                .build();
    }

    // Defaulting to merchant name since it is more descriptive than the field "description"
    public String getDescription() {
        return Objects.nonNull(merchant) ? merchant.getName() : description;
    }
}
