package se.tink.backend.aggregation.agents.authentication.options;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.libraries.authentication_options.AuthenticationOption;
import se.tink.libraries.authentication_options.AuthenticationOption.AuthenticationOptions;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;
import se.tink.libraries.authentication_options.AuthenticationOptionDto;
import se.tink.libraries.authentication_options.AuthenticationOptionField;
import se.tink.libraries.authentication_options.AuthenticationOptionsGroup;
import se.tink.libraries.authentication_options.AuthenticationOptionsGroupDto;
import se.tink.libraries.authentication_options.Field;
import se.tink.libraries.authentication_options.SupportedChannel;

@Slf4j
public class AuthenticationOptionsExtractor {

    private static final String DEFAULT_AGENT_PACKAGE_CLASS_PREFIX =
            "se.tink.backend.aggregation.agents";

    private final Reflections reflections = new Reflections(DEFAULT_AGENT_PACKAGE_CLASS_PREFIX);

    public AuthenticationOptionsExtractor() {
        validateAuthenticationOptions();
    }

    @VisibleForTesting
    AuthenticationOptionsExtractor(boolean doNotValidate) {
        log.warn("Instantiating AuthenticationOptionsExtractor without validating agents first.");
    }

    public Map<String, Set<AuthenticationOptionsGroupDto>> getAgentsAuthenticationOptions() {
        return reflections.getSubTypesOf(Agent.class).stream()
                .filter(hasAuthenticationOptions)
                .collect(Collectors.toMap(getAgentName, this::readAuthenticationOptions));
    }

    private final Predicate<Class<? extends Agent>> hasAuthenticationOptions =
            klass ->
                    klass.isAnnotationPresent(AuthenticationOptions.class)
                            || klass.isAnnotationPresent(AuthenticationOption.class);

    private final Function<Class<? extends Agent>, String> getAgentName =
            klass -> klass.getName().replace(DEFAULT_AGENT_PACKAGE_CLASS_PREFIX + ".", "");

    @VisibleForTesting
    Set<AuthenticationOptionsGroupDto> readAuthenticationOptions(Class<? extends Agent> klass) {
        AuthenticationOption[] authenticationOptions =
                klass.getAnnotationsByType(AuthenticationOption.class);
        Map<AuthenticationOptionsGroup, Set<AuthenticationOptionDto>>
                mapGroupsAuthenticationOptions = new HashMap<>();
        for (AuthenticationOption authenticationOption : authenticationOptions) {
            AuthenticationOptionDefinition definition = authenticationOption.definition();
            Set<Field> fields =
                    definition.getFields().stream()
                            .map(AuthenticationOptionField::getField)
                            .collect(Collectors.toSet());
            mapGroupsAuthenticationOptions.putIfAbsent(definition.getGroup(), new HashSet<>());
            mapGroupsAuthenticationOptions
                    .get(definition.getGroup())
                    .add(
                            AuthenticationOptionDto.newBuilder()
                                    .name(definition.name())
                                    .fields(fields)
                                    .displayText(definition.getDisplayText())
                                    .helpText(definition.getHelpText())
                                    .overallDefault(authenticationOption.overallDefault())
                                    .defaultForChannel(authenticationOption.defaultForChannel())
                                    .supportedChannels(definition.getSupportedChannels())
                                    .build());
        }

        return mapToDtos(mapGroupsAuthenticationOptions);
    }

    private Set<AuthenticationOptionsGroupDto> mapToDtos(
            Map<AuthenticationOptionsGroup, Set<AuthenticationOptionDto>>
                    mapGroupsAuthenticationOptions) {
        return mapGroupsAuthenticationOptions.entrySet().stream()
                .map(
                        e ->
                                AuthenticationOptionsGroupDto.newBuilder()
                                        .name(e.getKey().name())
                                        .helpText(e.getKey().getHelpText())
                                        .displayText(e.getKey().getDisplayText())
                                        .authenticationOptions(e.getValue())
                                        .build())
                .collect(Collectors.toSet());
    }

    private void validateAuthenticationOptions() {
        reflections.getSubTypesOf(Agent.class).stream()
                .filter(hasAuthenticationOptions)
                .forEach(this::validateAuthenticationOptions);
    }

    @VisibleForTesting
    void validateAuthenticationOptions(Class<? extends Agent> klass) {
        Boolean overallDefaultFound = false;
        Map<SupportedChannel, Boolean> defaultForChannelFound = new HashMap<>();
        Set<String> seenAuthenticationOptions = new HashSet();
        String agentName = getAgentName.apply(klass);

        AuthenticationOption[] authenticationOptions =
                klass.getAnnotationsByType(AuthenticationOption.class);
        for (AuthenticationOption authenticationOption : authenticationOptions) {
            if (seenAuthenticationOptions.contains(authenticationOption.definition().name())) {
                throw new IllegalStateException(
                        String.format(
                                "Agent %s has more than one authentication option of type %s",
                                agentName, authenticationOption.definition().name()));
            }
            seenAuthenticationOptions.add(authenticationOption.definition().name());

            if (authenticationOption.overallDefault()) {
                if (overallDefaultFound) {
                    throw new IllegalStateException(
                            String.format(
                                    "Agent %s has more than one authentication option with the flag overallDefault set to true",
                                    agentName));
                }
                overallDefaultFound = true;
            }

            SupportedChannel defaultForChannel = authenticationOption.defaultForChannel();
            if (defaultForChannel != SupportedChannel.NONE) {
                if (defaultForChannelFound.getOrDefault(defaultForChannel, false)) {
                    throw new IllegalStateException(
                            String.format(
                                    "Agent %s has more than one authentication option which is the default for channel %s",
                                    agentName, defaultForChannel.name()));
                } else if (!authenticationOption
                        .definition()
                        .getSupportedChannels()
                        .contains(defaultForChannel)) {
                    throw new IllegalStateException(
                            String.format(
                                    "Agent %s has an authentication option set as default for channel %s but that is not among its supported channels %s",
                                    agentName,
                                    defaultForChannel.name(),
                                    authenticationOption.definition().getSupportedChannels()));
                }
                defaultForChannelFound.put(defaultForChannel, true);
            }
        }
    }
}
