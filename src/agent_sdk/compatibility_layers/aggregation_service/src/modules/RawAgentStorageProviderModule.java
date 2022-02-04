package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Map;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.agent_sdk.runtime.src.storage.RawAgentStorage;

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
