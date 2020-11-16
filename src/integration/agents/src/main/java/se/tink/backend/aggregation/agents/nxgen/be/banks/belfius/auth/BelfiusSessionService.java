package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;

@AllArgsConstructor
public class BelfiusSessionService {

    private final AgentPlatformBelfiusApiClient apiClient;
    private final BelfiusProcessState processState;

    public void openSession() {
        openSession(processState.getMachineId());
    }

    public void openSession(String machineId) {
        SessionOpenedResponse sessionOpenedResponse = apiClient.openSession(machineId);
        processState.setSessionId(sessionOpenedResponse.getSessionId());
        if (StringUtils.isNotBlank(sessionOpenedResponse.getMachineIdentifier())) {
            processState.setMachineId(sessionOpenedResponse.getMachineIdentifier());
        }
        apiClient.startFlow(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }

    public void closeSession() {
        apiClient.closeSession(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }
}
