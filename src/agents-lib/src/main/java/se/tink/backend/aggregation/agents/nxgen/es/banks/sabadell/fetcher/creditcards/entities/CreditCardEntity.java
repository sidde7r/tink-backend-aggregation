package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class CreditCardEntity {
    private String bsprotect;
    private String description;
    private String isMarsans;
    private String name;
    private String productType;
    private String reference;
    private String type;
    private String shortDescription;
    private String number;
    private String realNumber;
    private String numcard;
    private boolean isOwner;
    private String activatedLE;
    private String codret;
    private String contractCCC;
    private String operativeCode;
    private String cvv2;
    private String dni;
    private String entity;
    private String mail;
    private String mailChecked;
    private String phoneNumber;
    private String phoneNumberChecked;
    private String alias;
    private String expirationDate;
    private AmountEntity balance;
    private AmountEntity availableBalance;
    private String dms;
    private String indAlert;
    private String selectableIndex;
    private boolean canActivate;
    private CardTypeEntity cardType;
    private String status;
    private List<Object> availableOperations;
    private boolean isInternational;
    private boolean isInternet;
    private boolean isSticker;
    private boolean stickerCard;
    private NfcCardEntity nfcCard;
    private String stickerPan;
    private boolean deteriorationLock;
    private boolean isPrepaidAnonymous;
    private List<DigitalCardEntity> digitalCard;

    @JsonIgnore
    public CreditCardAccount toTinkAccount() {
        String bankIdentifier = Hash.sha1AsHex(number);

        return CreditCardAccount.builder(number, balance.parseToNegativeTinkAmount(),
                availableBalance.parseToTinkAmount())
                .setAccountNumber(number)
                .setName(description)
                .setHolderName(new HolderName(name))
                .setBankIdentifier(bankIdentifier)
                .putInTemporaryStorage(bankIdentifier, this)
                .build();
    }

    public String getBsprotect() {
        return bsprotect;
    }

    public String getDescription() {
        return description;
    }

    public String getIsMarsans() {
        return isMarsans;
    }

    public String getName() {
        return name;
    }

    public String getProductType() {
        return productType;
    }

    public String getReference() {
        return reference;
    }

    public String getType() {
        return type;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getNumber() {
        return number;
    }

    public String getRealNumber() {
        return realNumber;
    }

    public String getNumcard() {
        return numcard;
    }

    @JsonProperty("isOwner")
    public boolean isOwner() {
        return isOwner;
    }

    public String getActivatedLE() {
        return activatedLE;
    }

    public String getCodret() {
        return codret;
    }

    public String getContractCCC() {
        return contractCCC;
    }

    public String getOperativeCode() {
        return operativeCode;
    }

    public String getCvv2() {
        return cvv2;
    }

    public String getDni() {
        return dni;
    }

    public String getEntity() {
        return entity;
    }

    public String getMail() {
        return mail;
    }

    public String getMailChecked() {
        return mailChecked;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhoneNumberChecked() {
        return phoneNumberChecked;
    }

    public String getAlias() {
        return alias;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public String getDms() {
        return dms;
    }

    public String getIndAlert() {
        return indAlert;
    }

    public String getSelectableIndex() {
        return selectableIndex;
    }

    public boolean isCanActivate() {
        return canActivate;
    }

    public CardTypeEntity getCardType() {
        return cardType;
    }

    public String getStatus() {
        return status;
    }

    public List<Object> getAvailableOperations() {
        return availableOperations;
    }

    @JsonProperty("isInternational")
    public boolean isInternational() {
        return isInternational;
    }

    @JsonProperty("isInternet")
    public boolean isInternet() {
        return isInternet;
    }

    @JsonProperty("isSticker")
    public boolean isSticker() {
        return isSticker;
    }

    public boolean isStickerCard() {
        return stickerCard;
    }

    public NfcCardEntity getNfcCard() {
        return nfcCard;
    }

    public String getStickerPan() {
        return stickerPan;
    }

    public boolean isDeteriorationLock() {
        return deteriorationLock;
    }

   @JsonProperty("isPrepaidAnonymous")
    public boolean isPrepaidAnonymous() {
        return isPrepaidAnonymous;
    }

    public List<DigitalCardEntity> getDigitalCard() {
        return digitalCard;
    }
}
