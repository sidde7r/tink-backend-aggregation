package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Map;
import se.tink.agent.runtime.storage.RawAgentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class RawAgentStorageProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public RawAgentStorage provideRawAgentStorage(CredentialsRequest credentialsRequest) {
        if (credentialsRequest == null) {
            return new RawAgentStorage(null);
        }

        Map<String, String> sensitivePayload =
                credentialsRequest.getCredentials().getSensitivePayloadAsMap();

        return new RawAgentStorage(sensitivePayload);
    }
}
