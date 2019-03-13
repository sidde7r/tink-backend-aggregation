package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartnersEntity {
  private String partnerType;

  private NatuarlPersonEntity naturalPerson;

  public boolean isNatural() {
    return partnerType.equalsIgnoreCase(DeutscheBankConstants.Accounts.PARTNER_TYPE_NATURAL);
  }

  public String getFullName() {
    return naturalPerson.getFullName();
  }
}
