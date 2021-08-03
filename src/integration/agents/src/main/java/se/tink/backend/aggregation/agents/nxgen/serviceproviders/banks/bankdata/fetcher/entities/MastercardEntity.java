package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MastercardEntity {
    private String agreementAccountRegNo;
    private String agreementAccountAccountNo;
    private String agreementName;
    private String agreementAccountOwner;
    private Double balanceTotal;
    private String balanceCurrency;
    private Double availableTotal;
    private String availableTotalCurrency;
    private Double maxBalanceTotal;
    private String maxBalanceTotalCurrency;
    private Double nextBillingTotal;
    private String billingForm;
    private String billingText;
    private String cardNo;
    private String cardUser;
    private String cardName;
    private Double balance;
    private Double maxBalance;
    private Double nextBilling;
    private Double available;
    private String cardStatus;
    private boolean stopped;
    private String latestUpdate;
    private String billingAccountRegNo;
    private String billingAccountAccountNo;
    private String cardUserCprNo;
}
