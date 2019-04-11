package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardMovementEntity {
    private String movementNumber;
    private String concept;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date date;

    private String hour;
    private String city;
    private boolean canSplit;
    private AmountEntity amount;
    private boolean indFracEnabled;
    private String indMov;
    private boolean isTraspasable;
    private AmountEntity commission;
    private AmountEntity originAmount;
    private String address;
    private boolean isConfirmed;
    private boolean isSplit;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount creditCardAccount) {
        return CreditCardTransaction.builder()
                .setCreditAccount(creditCardAccount)
                .setAmount(amount.parseToNegativeTinkAmount())
                .setDate(date)
                .setDescription(concept)
                .setPending(!isConfirmed)
                .build();
    }

    public String getMovementNumber() {
        return movementNumber;
    }

    public String getConcept() {
        return concept;
    }

    public Date getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public String getCity() {
        return city;
    }

    public boolean isCanSplit() {
        return canSplit;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public boolean isIndFracEnabled() {
        return indFracEnabled;
    }

    public String getIndMov() {
        return indMov;
    }

    public boolean isTraspasable() {
        return isTraspasable;
    }

    public AmountEntity getCommission() {
        return commission;
    }

    public AmountEntity getOriginAmount() {
        return originAmount;
    }

    public String getAddress() {
        return address;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public boolean isSplit() {
        return isSplit;
    }
}
