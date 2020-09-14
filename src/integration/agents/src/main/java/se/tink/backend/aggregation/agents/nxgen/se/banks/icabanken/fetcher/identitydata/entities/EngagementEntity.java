package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(UpperCamelCaseStrategy.class)
@Getter
public class EngagementEntity {
    private boolean hasEGiro;
    private boolean hasHomeLoan;
    private boolean hasUnsecuredPrivateLoan;
    private boolean hasFund;
    private boolean hasFundWithDisposition;
    private boolean hasInsuranceICA;
    private boolean hasInsuranceExternal;
    private boolean hasCard;
    private boolean hadActiveBank;
    private boolean hasActiveBank;
    private boolean hasAccount;
    private boolean hasAccountWithDisposition;
    private boolean hasActiveAccountsInActiveBank;
    private boolean hasCardICALoyalty;
    private boolean hasCardICALoyaltyDebitPayment;
}
