package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DistributionEntity {
    private String name;
    private double value;
    private int percent;
    private int no;
    private int productNo;
    private long accrualDate;
}
