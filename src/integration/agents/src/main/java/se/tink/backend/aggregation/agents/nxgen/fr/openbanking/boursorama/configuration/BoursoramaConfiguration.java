package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Data
public class BoursoramaConfiguration implements ClientConfiguration {

    @Secret private String qsealKeyUrl;
}
