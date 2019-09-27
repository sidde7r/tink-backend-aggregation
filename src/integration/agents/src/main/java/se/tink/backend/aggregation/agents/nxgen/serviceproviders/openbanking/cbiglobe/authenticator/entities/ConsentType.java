package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

public enum ConsentType {
  ACCOUNT("acc"),
  BALANCE_TRANSACTION("trans");

  private final String code;

  ConsentType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
