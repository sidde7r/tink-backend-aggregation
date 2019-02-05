package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OptInsEntity {
    private String balanceBeforeLogon;
    private String iceland;
    private String payByMobile;
    private String payconiq;
    private String thirdPartyTransfer;
    private String trustedTransfer;
    private String signByTwo;

    public String getBalanceBeforeLogon() {
        return balanceBeforeLogon;
    }

    public String getIceland() {
        return iceland;
    }

    public String getPayByMobile() {
        return payByMobile;
    }

    public String getPayconiq() {
        return payconiq;
    }

    public String getThirdPartyTransfer() {
        return thirdPartyTransfer;
    }

    public String getTrustedTransfer() {
        return trustedTransfer;
    }

    public String getSignByTwo() {
        return signByTwo;
    }
}
