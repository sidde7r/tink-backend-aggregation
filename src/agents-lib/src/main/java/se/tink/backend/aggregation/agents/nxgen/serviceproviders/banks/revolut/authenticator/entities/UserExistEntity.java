package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserExistEntity {
  private String registeredAs;
  private boolean premium;

  public String getRegisteredAs() {
    return registeredAs;
  }

  public boolean isPremium() {
    return premium;
  }
}
