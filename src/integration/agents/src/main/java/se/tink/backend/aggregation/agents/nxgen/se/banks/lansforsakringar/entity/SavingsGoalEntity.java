package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsGoalEntity {
  private String name;
  private String accountNumber;
  private String savingsGoalId;
  private double goalAmount;
  private double completedInPercent;
  // `sequenceNumber` is null - cannot define it!
  // `estimatedCompletionDate` is null - cannot define it!
  private String goalDate;
}