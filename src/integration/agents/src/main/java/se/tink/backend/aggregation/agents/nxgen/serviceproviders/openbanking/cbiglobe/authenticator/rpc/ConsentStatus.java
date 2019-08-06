package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

public enum ConsentStatus {
  received,
  rejected,
  valid,
  revokedByPsu,
  expired,
  terminatedByTpp,
  replaced,
  invalidated,
  pendingExpired;

  public boolean isAcceptedStatus() {
    return this == received || this == valid || this == revokedByPsu || this == replaced;
  }

}
