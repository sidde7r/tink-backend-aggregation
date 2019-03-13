package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NatuarlPersonEntity {
  private String firstName;

  private String lastName;

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
