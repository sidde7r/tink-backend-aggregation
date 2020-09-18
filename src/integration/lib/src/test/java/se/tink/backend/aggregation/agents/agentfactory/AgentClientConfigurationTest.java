package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.tools.ClientConfigurationMetaInfoHandler;
import se.tink.libraries.provider.ProviderDto;

public class AgentClientConfigurationTest {

    private static final Logger log = LoggerFactory.getLogger(AgentClientConfigurationTest.class);

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = new Throwable(throwable);
        while (rootCause != null) {
            if (rootCause instanceof IllegalArgumentException) {
                return rootCause;
            }
            rootCause = rootCause.getCause();
        }
        return throwable;
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private String createProperErrorMessageForAgentClientConfigurationError(Throwable e) {
        String errorMessagePrefix =
                "Could not fetch agent client configuration template and JsonSchema";

        Throwable rootCause = getRootCause(e);
        if (rootCause instanceof IllegalArgumentException) {
            return errorMessagePrefix
                    + " due to IllegalArgumentException exception. \nPlease ensure the followings: \n"
                    + "1) There is a subclass of ClientConfiguration class under the packages of the agent or the superclass of the agent\n"
                    + getStackTrace(e);
        } else {
            return errorMessagePrefix + "\n" + getStackTrace(e);
        }
    }

    // This method returns one provider for each agent
    // Only open banking agent is mandatory required a client configuration
    private Set<Provider> getProvidersForClientConfigurationTest(
            Set<Provider> providerConfigurations) {
        return providerConfigurations.stream()
                .filter(Provider::isOpenBanking)
                .filter(
                        provider ->
                                ProviderStatuses.ENABLED.equals(provider.getStatus())
                                        || ProviderStatuses.OBSOLETE.equals(provider.getStatus()))
                .filter(provider -> ProviderDto.ProviderTypes.TEST != provider.getType())
                // ThingsToDo: remove the filter of DE market after market team has added
                // client configuration implementation for all DE agents
                .filter(provider -> !"DE".equals(provider.getMarket()))
                .collect(groupingBy(Provider::getClassName))
                .entrySet()
                .stream()
                .map(
                        entry -> {
                            entry.getValue().sort(Comparator.comparing(Provider::getName));
                            return entry.getValue().get(0);
                        })
                .collect(Collectors.toSet());
    }

    @Test
    public void testFindClosestClientConfigurationClass() {
        Set<Provider> providerConfigurations =
                new ProviderReader()
                        .getProviderConfigurations(
                                "external/tink_backend/src/provider_configuration/data/seeding");

        Set<Provider> obProviders = getProvidersForClientConfigurationTest(providerConfigurations);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                obProviders.stream()
                                        .map(ClientConfigurationMetaInfoHandler::new)
                                        .forEach(
                                                ClientConfigurationMetaInfoHandler
                                                        ::findClosestClientConfigurationClass));

        // then
        if (throwable != null) {
            log.error(createProperErrorMessageForAgentClientConfigurationError(throwable));
        }
        assertNull(throwable);
    }
}
