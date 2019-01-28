package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssociatedAccountEntity {
  private String id;

  public String getId() {
    return id;
  }
}
