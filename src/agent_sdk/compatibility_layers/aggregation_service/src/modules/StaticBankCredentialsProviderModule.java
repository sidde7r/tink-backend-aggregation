package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.HashMap;
import se.tink.agent.runtime.operation.StaticBankCredentialsImpl;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class StaticBankCredentialsProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public StaticBankCredentials provideStaticBankCredentials(
            CredentialsRequest credentialsRequest) {
        return mapToStaticBankCredentials(credentialsRequest);
    }

    public static StaticBankCredentials mapToStaticBankCredentials(
            CredentialsRequest credentialsRequest) {

        if (credentialsRequest == null) {
            return null;
        }

        Credentials credentials = credentialsRequest.getCredentials();
        if (credentials == null) {
            return new StaticBankCredentialsImpl(new HashMap<>());
        }

        return new StaticBankCredentialsImpl(credentials.getFields());
    }
}
