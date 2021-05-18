package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FollowingPaymentEntity {
    private double instalment;
    private double interest;
    private int fees;
    private double total;
    private String date;
}
