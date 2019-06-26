package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationRequest {

  private String access;


  public AuthorizationRequest(String access) {
    this.access = access;
  }
}
