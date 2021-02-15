package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import java.time.Clock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;

@Data
@Slf4j
public class LunarNemIdParametersFetcher implements NemIdParametersFetcher {

    private static final String NEM_ID_SCRIPT_FORMAT =
            "<script type=\"text/x-nemid\" id=\"nemid_parameters\">%s</script>";

    private final Clock clock;
    private String nemIdParameters;

    @Override
    public NemIdParameters getNemIdParameters() throws AuthenticationException {
        return new NemIdParameters(
                String.format(NEM_ID_SCRIPT_FORMAT, nemIdParameters)
                        + String.format(
                                NemIdConstants.NEM_ID_IFRAME_FORMAT,
                                NemIdConstants.NEM_ID_INIT_URL + clock.instant().toEpochMilli()));
    }
}
