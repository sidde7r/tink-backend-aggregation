package se.tink.backend.aggregation.agents.agentplatform.authentication;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public class ThirdPartyRequestWithStateParam implements ThirdPartyRequest {

    private static final String STRONG_AUTHENTICATION_QUERY_PARAM_KEY = "state";

    private URL url;
    private String strongAuthenticationState;
    private final long waitFor;
    private final TimeUnit timeUnit;

    public ThirdPartyRequestWithStateParam(String url, long waitFor, TimeUnit timeUnit) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Redirect url has incorrect format");
        }
        initStrongAuthenticationState();
        this.waitFor = waitFor;
        this.timeUnit = timeUnit;
    }

    public ThirdPartyAppAuthenticationPayload getPayload() {
        return ThirdPartyAppAuthenticationPayload.of(
                new se.tink.backend.aggregation.nxgen.http.url.URL(url.toString()));
    }

    public SupplementalWaitRequest getSupplementalWaitRequest() {
        return new SupplementalWaitRequest(strongAuthenticationState, waitFor, timeUnit);
    }

    private void initStrongAuthenticationState() {
        strongAuthenticationState =
                parseFromUrlIfPresent().orElseGet(this::generateNewStrongAuthenticationState);
    }

    private Optional<String> parseFromUrlIfPresent() {
        Map<String, String> urlQueryMap =
                Optional.ofNullable(url.getQuery())
                        .map(
                                query ->
                                        Arrays.stream(query.split("&"))
                                                .map(param -> param.split("="))
                                                .collect(Collectors.toMap(e -> e[0], e -> e[1])))
                        .orElse(Collections.emptyMap());
        return Optional.ofNullable(urlQueryMap.get(STRONG_AUTHENTICATION_QUERY_PARAM_KEY));
    }

    private String generateNewStrongAuthenticationState() {
        final String newStrongAuthenticationState =
                StrongAuthenticationState.generateUuidWithTinkTag();
        try {
            url =
                    new URIBuilder(url.toString())
                            .addParameter(
                                    STRONG_AUTHENTICATION_QUERY_PARAM_KEY,
                                    newStrongAuthenticationState)
                            .build()
                            .toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalStateException("Redirect url has incorrect format");
        }
        return newStrongAuthenticationState;
    }
}
