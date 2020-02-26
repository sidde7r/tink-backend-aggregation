package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingEntity {
    private AgeEntity paymentAge;
    private String paymentDate;
    private String paymentType;
    private double insuranceAmount;
    private String insuranceAmountType;
    private double investmentValue;
}
