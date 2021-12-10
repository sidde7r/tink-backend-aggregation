package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngApiInputData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngUserAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseTinkClientConfigurator;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public final class IngBaseAuthenticationTestFixture {

    public TinkHttpClient createTinkHttpClient(Filter mockFilter) {
        TinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        new IngBaseTinkClientConfigurator().configureClient(client, 1, 1);
        client.addFilter(mockFilter);

        return client;
    }

    public CredentialsRequest createCredentialsRequest(
            Credentials credentials, boolean userIsAvailable) {
        return new RefreshInformationRequest.Builder()
                .credentials(credentials)
                .userAvailability(createUserAvailability(userIsAvailable))
                .forceAuthenticate(userIsAvailable)
                .build();
    }

    private UserAvailability createUserAvailability(boolean userIsAvailable) {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setOriginatingUserIp("111.0.0.0");
        userAvailability.setUserPresent(userIsAvailable);
        userAvailability.setUserAvailableForInteraction(userIsAvailable);

        return userAvailability;
    }

    public AgentComponentProvider prepareAgentComponentProvider() {
        AgentComponentProvider agentComponentProvider = mock(AgentComponentProvider.class);
        given(agentComponentProvider.getRandomValueGenerator())
                .willReturn(new MockRandomValueGenerator());
        given(agentComponentProvider.getLocalDateTimeSource())
                .willReturn(new ConstantLocalDateTimeSource());
        return agentComponentProvider;
    }

    public IngApiInputData createIngApiInputDataMock(CredentialsRequest credentialsRequest) {
        return IngApiInputData.builder()
                .userAuthenticationData(createIngUserAuthenticationData())
                .strongAuthenticationState(createStrongAuthenticationState())
                .credentialsRequest(credentialsRequest)
                .build();
    }

    public IngUserAuthenticationData createIngUserAuthenticationData() {
        return new IngUserAuthenticationData(true, null);
    }

    public StrongAuthenticationState createStrongAuthenticationState() {
        return new StrongAuthenticationState("testing_state");
    }

    @SneakyThrows
    public <T> T deserializeFromFile(String fileName, Class<T> className) {
        String resourcesPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/authenticator/resources/";

        return SerializationUtils.deserializeFromString(
                Paths.get(resourcesPath, fileName).toFile(), className);
    }
}
