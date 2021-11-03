package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

@Ignore
public class SibsAuthenticatorTestFixtures {

    static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs/authenticator/resources/";
    static final String STRONG_AUTHENTICATION_STATE = "test_state";

    private RefreshScope getRefreshScope(Set<FinancialServiceSegment> financialServiceSegment) {
        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(financialServiceSegment);
        return refreshScope;
    }

    SibsAuthenticator sibsAuthenticatorWith(
            CredentialsRequest credentialsRequest,
            SibsBaseApiClient sibsBaseApiClient,
            SibsUserState sibsUserState) {
        return new SibsAuthenticator(
                sibsBaseApiClient,
                sibsUserState,
                credentialsRequest,
                new StrongAuthenticationState(STRONG_AUTHENTICATION_STATE),
                new ConstantLocalDateTimeSource());
    }

    CredentialsRequest getCredentialsRequestWithoutRefreshScope() throws IOException {
        return new ManualAuthenticateRequest(
                getSibsUser(),
                getProviderConfiguration(),
                getSibsCredentials(),
                getUserAvailability());
    }

    CredentialsRequest credentialsRequestWithScope(
            Set<FinancialServiceSegment> financialServiceSegment) throws IOException {
        RefreshScope refreshScope = getRefreshScope(financialServiceSegment);
        ManualAuthenticateRequest credentialsRequest =
                new ManualAuthenticateRequest(
                        getSibsUser(),
                        getProviderConfiguration(),
                        getSibsCredentials(),
                        getUserAvailability());
        credentialsRequest.setRefreshScope(refreshScope);
        return credentialsRequest;
    }

    UserAvailability getUserAvailability() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);
        userAvailability.setUserAvailableForInteraction(true);
        userAvailability.setOriginatingUserIp("127.0.0.1");
        return userAvailability;
    }

    Provider getProviderConfiguration() {
        return new ProviderReader()
                        .getProviderConfigurations(
                                "external/tink_backend/src/provider_configuration/data/seeding")
                        .stream()
                        .findFirst()
                        .get();
    }

    Credentials getSibsCredentials() throws IOException {
        return getFileContent("sibs_credentials_template.json", Credentials.class);
    }

    User getSibsUser() throws IOException {
        return getFileContent("sibs_user_template.json", User.class);
    }

    AgentConfiguration<SibsConfiguration> getSibsAgentConfiguration() throws IOException {
        SibsConfiguration sibsConfiguration =
                getFileContent("sibs_configuration.json", SibsConfiguration.class);
        return new AgentConfiguration.Builder<SibsConfiguration>()
                .setProviderSpecificConfiguration(sibsConfiguration)
                .setRedirectUrl("https://test.tink.com")
                .build();
    }

    <T> T getFileContent(String fileName, Class<T> className) throws IOException {
        String consentResponse =
                new String(
                        Files.readAllBytes(Paths.get(RESOURCES_PATH).resolve(fileName)),
                        StandardCharsets.UTF_8);
        return SerializationUtils.deserializeFromString(consentResponse, className);
    }
}
