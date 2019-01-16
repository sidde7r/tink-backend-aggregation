package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LanguageEntity {
  @JsonProperty("IDIOMA_ISO")
  private String language = SantanderConstants.QUERYPARAMS.LANGUAGE_DE;

  @JsonProperty("DIALECTO_ISO")
  private String dialect = SantanderConstants.QUERYPARAMS.DIALECT_DE;
}
