package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.AgentPlatformLunarApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@Slf4j
public class LunarNemIdParametrsFetcher implements NemIdParametersFetcher {

    private static final String NEM_ID_SCRIPT_FORMAT =
            "<script type=\"text/x-nemid\" id=\"nemid_parameters\">%s</script>";

    private final LunarProcessState processState;
    private final AgentPlatformLunarApiClient apiClient;
    private final String deviceId;

    @Override
    public NemIdParameters getNemIdParameters() throws AuthenticationException {
        if (StringUtils.isBlank(processState.getNemIdParameters())
                || StringUtils.isBlank(processState.getChallenge())) {
            NemIdParamsResponse nemIdParamsResponse = getNemIdParamsResponse();
            processState.setChallenge(nemIdParamsResponse.getChallenge());
            processState.setNemIdParameters(
                    SerializationUtils.serializeToString(nemIdParamsResponse));
        }

        return new NemIdParameters(
                String.format(NEM_ID_SCRIPT_FORMAT, processState.getNemIdParameters())
                        + String.format(
                                NemIdConstants.NEM_ID_IFRAME,
                                NemIdConstants.NEM_ID_INIT_URL + Instant.now().toEpochMilli()));
    }

    private NemIdParamsResponse getNemIdParamsResponse() {
        try {
            return apiClient.getNemIdParameters(deviceId);
        } catch (HttpResponseException e) {
            log.error("Could not get NemId Iframe parameters");
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }
}
