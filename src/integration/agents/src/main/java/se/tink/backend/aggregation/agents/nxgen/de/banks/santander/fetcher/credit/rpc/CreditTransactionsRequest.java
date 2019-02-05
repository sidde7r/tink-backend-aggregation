package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.Entrada;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.GetCardMovements;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.Lang;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditTransactionsRequest {

  @JsonProperty("getCardMovements")
  private GetCardMovements getCardMovements;

  public CreditTransactionsRequest(
      String contractID, String pan, String indMPX, String dIALECTOISO, String iDIOMAISO) {
    getCardMovements =
        new GetCardMovements(
            new Entrada(contractID, new Lang(dIALECTOISO, iDIOMAISO), pan, indMPX));
  }
}
