package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsGoalEntity {
    private String id;
    private String name;
    private double amount;
    private String endDate;
    // `category` is null - cannot define it!
    // `imageReference` is null - cannot define it!
    private int chosenRisk;
    private String status;
}
