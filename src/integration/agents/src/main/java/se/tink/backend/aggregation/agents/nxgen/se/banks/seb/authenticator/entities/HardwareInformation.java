package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.RequestComponent;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HardwareInformation implements RequestComponent {}
