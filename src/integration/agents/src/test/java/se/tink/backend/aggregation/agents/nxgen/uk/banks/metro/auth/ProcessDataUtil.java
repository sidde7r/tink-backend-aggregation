package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth;

import java.util.Collections;
import java.util.function.Supplier;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public interface ProcessDataUtil {

    static AgentProceedNextStepAuthenticationRequest nextStepAuthRequest(
            Supplier<MetroProcessState> processStateObject,
            Supplier<MetroAuthenticationData> persistedDataObject) {
        String persistedDataAsStringJson =
                SerializationUtils.serializeToString(persistedDataObject.get());
        String processStateAsStringJson =
                SerializationUtils.serializeToString(processStateObject.get());

        AgentAuthenticationProcessState processState =
                AgentAuthenticationProcessState.of("MetroProcessState", processStateAsStringJson);
        AgentAuthenticationPersistedData persistedData =
                AgentAuthenticationPersistedData.of(
                        "METRO_AUTHENTICATION_DATA", persistedDataAsStringJson);
        return new AgentProceedNextStepAuthenticationRequest(
                null, processState, persistedData, null);
    }

    static AgentUserInteractionAuthenticationProcessRequest userInteractionAuthRequest(
            Supplier<MetroProcessState> processStateObject,
            Supplier<MetroAuthenticationData> persistedDataObject,
            Supplier<AgentFieldValue> intractableFields) {
        String persistedDataAsStringJson =
                SerializationUtils.serializeToString(persistedDataObject.get());
        String processStateAsStringJson =
                SerializationUtils.serializeToString(processStateObject.get());

        AgentAuthenticationProcessState processState =
                AgentAuthenticationProcessState.of("MetroProcessState", processStateAsStringJson);
        AgentAuthenticationPersistedData persistedData =
                AgentAuthenticationPersistedData.of(
                        "METRO_AUTHENTICATION_DATA", persistedDataAsStringJson);

        return new AgentUserInteractionAuthenticationProcessRequest(
                null,
                persistedData,
                processState,
                Collections.singletonList(intractableFields.get()),
                null);
    }
}
