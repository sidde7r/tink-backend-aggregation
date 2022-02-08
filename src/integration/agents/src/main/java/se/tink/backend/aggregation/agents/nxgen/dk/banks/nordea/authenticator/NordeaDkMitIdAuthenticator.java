package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import static java.util.Arrays.asList;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocatorsElements;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxyResponseMatchers;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;

@Slf4j
@RequiredArgsConstructor
public class NordeaDkMitIdAuthenticator implements MitIdAuthenticator {

    private static final List<MitIdLocator>
            LOCATORS_THAT_SHOULD_NOT_BE_WRAPPED_IN_ADDITIONAL_IFRAME =
                    asList(MitIdLocator.LOC_CPR_INPUT, MitIdLocator.LOC_CPR_BUTTON_OK);

    private final NordeaDkAuthenticatorUtils authenticatorUtils;
    private final NordeaDkApiClient bankClient;

    @Override
    public void initializeMitIdWindow(WebDriverService driverService) {
        OAuthSessionData sessionData = authenticatorUtils.prepareOAuthSessionData();
        URL openMitIdUrl =
                bankClient.getInitOauthUrl(
                        sessionData.getCodeChallenge(),
                        sessionData.getState(),
                        sessionData.getNonce(),
                        NordeaDkConstants.QueryParamValues.MIT_ID_LOGIN_HINT);

        driverService.get(openMitIdUrl.toString());
    }

    @Override
    public MitIdLocatorsElements getLocatorsElements() {

        MitIdLocatorsElements locatorsElements = new MitIdLocatorsElements();
        locatorsElements.applyModifier(
                (mitIdLocator, elementLocator) -> {
                    if (!LOCATORS_THAT_SHOULD_NOT_BE_WRAPPED_IN_ADDITIONAL_IFRAME.contains(
                            mitIdLocator)) {
                        return elementLocator
                                .toBuilder()
                                .topmostIframe(By.tagName("iframe"))
                                .build();
                    }
                    return elementLocator;
                });
        return locatorsElements;
    }

    @Override
    public ProxySaveResponseMatcher getMatcherForAuthenticationFinishResponse() {
        return new ProxyResponseMatchers.ProxyUrlSubstringMatcher(
                NordeaDkConstants.MitId.AUTH_FINISH_REDIRECT_URL_SUBSTRING);
    }

    @Override
    public void finishAuthentication(MitIdAuthenticationResult authenticationResult) {
        String redirectUrl =
                authenticationResult.getProxyResponse().getResponse().headers().get("Location");
        String authorizationCode = extractAuthorizationCode(redirectUrl);

        authenticatorUtils.exchangeOauthToken(authorizationCode);
    }

    private String extractAuthorizationCode(String redirectUrl) {
        List<NameValuePair> params =
                URLEncodedUtils.parse(URI.create(redirectUrl), StandardCharsets.UTF_8);
        return params.stream()
                .filter(x -> "code".equalsIgnoreCase(x.getName()))
                .findFirst()
                .map(NameValuePair::getValue)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot extract authorization code from proxy response"));
    }

    @Override
    public boolean isAuthenticationFinishedWithAgentSpecificError(WebDriverService driverService) {
        return driverService
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(NordeaDkConstants.MitId.LOC_ERROR_PAGE_MESSAGE)
                                .searchOnlyOnce()
                                .build())
                .isNotEmpty();
    }

    @Override
    public void handleAuthenticationFinishedWithAgentSpecificError(WebDriverService driverService) {
        String errorMessage =
                driverService
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(NordeaDkConstants.MitId.LOC_ERROR_PAGE_MESSAGE)
                                        .searchOnlyOnce()
                                        .build())
                        .getFirstFoundElement()
                        .map(element -> element.getAttribute("textContent"))
                        .orElse(null);
        if (errorMessage == null) {
            throw new IllegalStateException("Cannot find MitID authentication error message");
        }

        AgentError agentError =
                tryMatchAgentSpecificErrorForMessage(errorMessage)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Unknown MitID authentication error message"));
        throw agentError.exception();
    }

    private static Optional<AgentError> tryMatchAgentSpecificErrorForMessage(String errorMessage) {
        return NordeaDkConstants.MitId.ERROR_MESSAGE_MAPPING.entries().stream()
                .filter(entry -> StringUtils.containsIgnoreCase(errorMessage, entry.getValue()))
                .findFirst()
                .map(Map.Entry::getKey);
    }
}
