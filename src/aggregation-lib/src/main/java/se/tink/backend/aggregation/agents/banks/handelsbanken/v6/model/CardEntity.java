package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEntity extends AbstractResponse {
    private static final ImmutableSet<String> INVERTED_CARD_TYPES = ImmutableSet.of(
            "H", // BUSINESS_VISA
            "I" // // BUSINESS_PRIVATE_VISA
    );
    private static final ImmutableSet<String> INACTIVE_CARD_STATUSES = ImmutableSet.of(
            // "10", // Inactive
            // "9" // Cancelled
            );

    private boolean abroadOpen;
    private AmountEntity amountAvailable;
    private String arrangementName;
    private AmountEntity balance;
    private boolean internetOpen;
    private String name;
    private String numberMasked;
    private String statusText;

    private String typeCode;

    public AmountEntity getAmountAvailable() {
        return amountAvailable;
    }

    public String getArrangementName() {
        return arrangementName;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }

    public String getNumberMasked() {
        return numberMasked;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public boolean hasInvertedTransactions() {
        return (INVERTED_CARD_TYPES.contains(typeCode));
    }

    public boolean isAbroadOpen() {
        return abroadOpen;
    }

    public boolean isInactive() {
        return (INACTIVE_CARD_STATUSES.contains(statusText));
    }

    public boolean isInternetOpen() {
        return internetOpen;
    }

    public void setAbroadOpen(boolean abroadOpen) {
        this.abroadOpen = abroadOpen;
    }

    public void setAmountAvailable(AmountEntity amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public void setArrangementName(String arrangementName) {
        this.arrangementName = arrangementName;
    }

    public void setBalance(AmountEntity balance) {
        this.balance = balance;
    }

    public void setInternetOpen(boolean internetOpen) {
        this.internetOpen = internetOpen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberMasked(String numberMasked) {
        this.numberMasked = numberMasked;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }
}
