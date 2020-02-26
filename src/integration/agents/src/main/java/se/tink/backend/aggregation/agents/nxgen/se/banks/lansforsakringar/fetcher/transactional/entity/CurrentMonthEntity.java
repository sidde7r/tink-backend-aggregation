package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrentMonthEntity {
  private String year;
  private String month;
  private double sumUpcomingTransactions;
  private String currentDate;
}
