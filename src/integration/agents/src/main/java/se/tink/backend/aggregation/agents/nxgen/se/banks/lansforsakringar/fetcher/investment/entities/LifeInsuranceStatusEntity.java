package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LifeInsuranceStatusEntity {
    private boolean isFundSwitchAllowed;
    private boolean isFuturePremiumPlanAllowed;
    private boolean isOngoingPremium;
    private boolean isOngoingFundTrade;
    private boolean hasPaymentInformation;
    private String assetReallocationAllowed;
    private boolean isTermsAndConditionsApplicable;
    private boolean isFundSwitchMayPostponePayout;
    private boolean hasTransactions;
}
