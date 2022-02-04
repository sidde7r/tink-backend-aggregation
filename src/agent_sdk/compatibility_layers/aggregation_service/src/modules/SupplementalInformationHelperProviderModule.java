package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.agent.sdk.utils.SupplementalInformationHelper;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.agent_sdk.runtime.src.utils.SupplementalInformationHelperImpl;

public class SupplementalInformationHelperProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public SupplementalInformationHelper provideSupplementalInformationHelper(
            CredentialsRequest request) {
        Provider provider = request.getProvider();
        Map<String, Field> fieldMap =
                provider.getSupplementalFields().stream()
                        .collect(Collectors.toMap(Field::getName, field -> field));

        return new SupplementalInformationHelperImpl(fieldMap);
    }
}
