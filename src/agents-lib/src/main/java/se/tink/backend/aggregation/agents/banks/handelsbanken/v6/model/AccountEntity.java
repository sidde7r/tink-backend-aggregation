package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.SHBUtils;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity extends AbstractResponse implements GeneralAccountEntity {
    private AmountEntity amountAvailable;
    private AmountEntity balance;
    private String clearingNumber;
    private boolean displayBalance;
    private String holderName;
    private boolean isCard;
    private String name;
    private String number;
    private String numberFormatted;

    public AmountEntity getAmountAvailable() {
        return amountAvailable;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getNumberFormatted() {
        return numberFormatted;
    }

    public boolean isCard() {
        return isCard;
    }

    public boolean isDisplayBalance() {
        return displayBalance;
    }

    public void setAmountAvailable(AmountEntity amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public void setBalance(AmountEntity balance) {
        this.balance = balance;
    }

    public void setCard(boolean isCard) {
        this.isCard = isCard;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public void setDisplayBalance(boolean displayBalance) {
        this.displayBalance = displayBalance;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setNumberFormatted(String numberFormatted) {
        this.numberFormatted = numberFormatted;
    }

    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier.Type type = SHBUtils.getAccountIdentifierType(getNumberFormatted());
        if (type == AccountIdentifier.Type.SE) {
            return new SwedishIdentifier(number);
        } else {
            return new SwedishSHBInternalIdentifier(number);
        }
    }

    @Override
    public String generalGetBank() {
        AccountIdentifier identifier = generalGetAccountIdentifier();
        if (identifier.isValid() && identifier.getType() == AccountIdentifier.Type.SE_SHB_INTERNAL) {
            return "Handelsbanken";
        } else if (identifier.isValid()) {
            return generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    @Override
    public String generalGetName() {
        return getName();
    }
}
