package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Optional;

@JsonObject
public class HeaderEntity {
  @JsonProperty("ResponseId")
  private String responseId;

  @JsonProperty("OpToken")
  private String opToken;

  @JsonProperty("Time")
  private String team;

  @JsonProperty("SessionTimeout")
  private int sessionTimeout;

  @JsonProperty("Status")
  private StatusEntity status;

  @JsonProperty("Contexto")
  private ContextEntity context;

  public StatusEntity getStatus() {
    return status;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public String getOpToken() {
    return opToken;
  }

  public ContextEntity getContext() {
    return context;
  }

  public Integer getResultCode() {
    return Optional.ofNullable(getStatus())
            .map(StatusEntity::getCode)
            .orElse(null);
  }
}
