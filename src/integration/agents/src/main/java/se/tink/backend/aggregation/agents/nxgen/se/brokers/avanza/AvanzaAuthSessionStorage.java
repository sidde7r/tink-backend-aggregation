package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class AvanzaAuthSessionStorage {
    private final SessionStorage sessionStorage;

    public Optional<String> getSecurityToken(String authSession) {
        return sessionStorage.keySet().stream()
                .filter(
                        key ->
                                key.matches(
                                        String.format(
                                                AvanzaConstants.StorageKeys.SECURITY_TOKEN_FORMAT,
                                                authSession)))
                .findFirst();
    }

    public List<String> getAuthSessions() {
        return sessionStorage.keySet().stream()
                .filter(
                        key ->
                                key.matches(
                                        String.format(
                                                AvanzaConstants.StorageKeys.AUTH_SESSION_FORMAT,
                                                ".+"))) // regex matches on "auth_session:.+
                .map(key -> key.split(":")[1])
                .collect(Collectors.toList());
    }
}
