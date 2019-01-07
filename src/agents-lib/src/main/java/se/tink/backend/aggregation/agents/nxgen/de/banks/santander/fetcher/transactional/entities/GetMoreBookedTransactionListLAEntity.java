package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetMoreBookedTransactionListLAEntity {

  @JsonProperty("filter")
  private FilterEntity filter;

  @JsonProperty("initialList")
  private InitialListEntity initialList;

  @JsonProperty("profile")
  private ProfileEntity profile;

  public GetMoreBookedTransactionListLAEntity(
      Date from, Date to, String localContractType, String localContractDetail, String company) {
    filter = new FilterEntity(from, to);
    initialList = new InitialListEntity(localContractType, localContractDetail);
    profile = new ProfileEntity(company);
  }
}
