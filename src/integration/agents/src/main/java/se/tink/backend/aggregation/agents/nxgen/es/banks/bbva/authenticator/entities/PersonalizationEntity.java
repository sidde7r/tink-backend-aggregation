package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PersonalizationEntity {
  private String channelCode;
  private String personalizationCode;
}
