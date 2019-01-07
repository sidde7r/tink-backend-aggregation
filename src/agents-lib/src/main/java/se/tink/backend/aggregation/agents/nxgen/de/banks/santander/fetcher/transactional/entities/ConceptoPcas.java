package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConceptoPcas {

  @JsonProperty("VALOR_CONCEPTO")
  private String vALORCONCEPTO;

  @JsonProperty("EMPRESA")
  private String eMPRESA;

  @JsonProperty("COD_CONCEPTO_PCAS")
  private String cODCONCEPTOPCAS;
}
