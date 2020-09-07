package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class PhaseDto {

    private int retryCounter;

    private String securityLevel;

    private boolean fallbackFactorAvailable;

    private String state;

    private String previousResult;
}
