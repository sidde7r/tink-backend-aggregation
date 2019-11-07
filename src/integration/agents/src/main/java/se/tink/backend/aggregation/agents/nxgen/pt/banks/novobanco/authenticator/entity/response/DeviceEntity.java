package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceEntity {
  @JsonProperty("Id")
  private String id;

  public String getId() {
    return id;
  }
}
