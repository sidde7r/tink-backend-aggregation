package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataCardEntity {
    private BankdataCardIdEntity cardKeyJson;
    private BankdataAccountIdEntity account;
    private String cardName;
    private String imageEnum;
    private String cardStatus;
    private String expirationDate;

    public BankdataCardIdEntity getCardKeyJson() {
        return cardKeyJson;
    }

    public BankdataAccountIdEntity getAccount() {
        return account;
    }

    public String getCardName() {
        return cardName;
    }

    public String getImageEnum() {
        return imageEnum;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
}
