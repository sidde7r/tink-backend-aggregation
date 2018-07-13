package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsEntity {
    private String productCode;
    private String cardName;
    private String cardNr;
    private String contractNr;
    private String cardStatus;
    private String embossName;
    private String expire;
    private String memberNr;
    private String accountNr;
    private String authorizationRegionId;
    private Boolean vbv;
    private Boolean cardRenewal;
    private Boolean pinReorderOk;
    private Boolean modifyLimits;
    private Boolean modifyRegions;
    private Boolean movableToAccount;
    private Boolean paymentFreeMonthsChangeAllowed;
    private String cardShortName;
    private Boolean transferable;
    private Boolean showLimits;
    private String type;

    public String getProductCode() {
        return productCode;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardNr() {
        return cardNr;
    }

    public String getContractNr() {
        return contractNr;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public String getEmbossName() {
        return embossName;
    }

    public String getExpire() {
        return expire;
    }

    public String getMemberNr() {
        return memberNr;
    }

    public String getAccountNr() {
        return accountNr;
    }

    public String getAuthorizationRegionId() {
        return authorizationRegionId;
    }

    public Boolean getVbv() {
        return vbv;
    }

    public Boolean getCardRenewal() {
        return cardRenewal;
    }

    public Boolean getPinReorderOk() {
        return pinReorderOk;
    }

    public Boolean getModifyLimits() {
        return modifyLimits;
    }

    public Boolean getModifyRegions() {
        return modifyRegions;
    }

    public Boolean getMovableToAccount() {
        return movableToAccount;
    }

    public Boolean getPaymentFreeMonthsChangeAllowed() {
        return paymentFreeMonthsChangeAllowed;
    }

    public String getCardShortName() {
        return cardShortName;
    }

    public Boolean getTransferable() {
        return transferable;
    }

    public Boolean getShowLimits() {
        return showLimits;
    }

    public String getType() {
        return type;
    }
}
