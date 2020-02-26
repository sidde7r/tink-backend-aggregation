package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundEntity {
  private String name;
  private String fundId;
  private String isinCode;
  private String managementForm;
  private String fundInfoURL;
  private CategoryEntity category;
}