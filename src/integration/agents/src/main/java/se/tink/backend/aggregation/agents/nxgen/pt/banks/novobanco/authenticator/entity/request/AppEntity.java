package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AppEntity {
  @JsonProperty("Version")
  private String version = FieldValues.APP_VERSION;

  @JsonProperty("Build")
  private String build = FieldValues.APP_BUILD;
}
