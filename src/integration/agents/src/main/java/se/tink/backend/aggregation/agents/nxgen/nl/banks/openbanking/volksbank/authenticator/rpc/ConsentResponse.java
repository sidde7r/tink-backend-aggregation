package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ConsentResponse {

    @Getter private String consentId;
}
