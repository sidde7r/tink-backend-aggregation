package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsEntity {

  @JsonProperty("listTarjetas")
  private List<ListTarjetasItem> listTarjetas;
}
