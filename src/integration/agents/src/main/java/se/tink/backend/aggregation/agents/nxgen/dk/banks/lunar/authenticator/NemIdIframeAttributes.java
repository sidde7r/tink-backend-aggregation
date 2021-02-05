package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import lombok.Data;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;

@Data
public class NemIdIframeAttributes {
    private final NemIdIFrameController nemIdIFrameController;
    private final Credentials credentials;
    private final LunarNemIdParametersFetcher parametersFetcher;
}
