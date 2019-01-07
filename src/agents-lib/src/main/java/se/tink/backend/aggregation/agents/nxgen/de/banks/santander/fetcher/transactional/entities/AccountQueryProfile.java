package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountQueryProfile {
  private String company = SantanderConstants.QUERYPARAMS.COMPANY_ID;
  private String channel = SantanderConstants.QUERYPARAMS.CHANNEL;
  private LanguageEntity language = new LanguageEntity();
}
