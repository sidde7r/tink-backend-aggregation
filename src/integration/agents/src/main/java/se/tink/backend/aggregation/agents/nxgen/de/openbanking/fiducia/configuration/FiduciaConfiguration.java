package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
public class FiduciaConfiguration implements ClientConfiguration {

    @Secret private String certificate;
    @Secret private String tppId;

    public FiduciaConfiguration validateConfig() {
        requireNonNull(Strings.emptyToNull(certificate));
        requireNonNull(Strings.emptyToNull(tppId));

        return this;
    }
}
