package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.GetCardDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.LangEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetailsRequest {

  @JsonProperty("getCardDetails")
  private GetCardDetailsEntity getCardDetails;

  public CardDetailsRequest(String contactId, String dIALECTOISO, String iDIOMAISO, String pan) {
    this.getCardDetails =
        new GetCardDetailsEntity(contactId, new LangEntity(dIALECTOISO, iDIOMAISO), pan);
  }
}
