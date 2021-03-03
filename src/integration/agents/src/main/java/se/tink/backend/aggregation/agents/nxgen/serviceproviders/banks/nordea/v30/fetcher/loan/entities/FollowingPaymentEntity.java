package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FollowingPaymentEntity {
    private double instalment;
    private double interest;
    private int fees;
    private double total;
    private String date;
}
