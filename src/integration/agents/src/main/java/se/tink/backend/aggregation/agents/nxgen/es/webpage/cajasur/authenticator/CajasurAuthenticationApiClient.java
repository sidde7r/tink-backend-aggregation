package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import java.awt.image.BufferedImage;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.LoginRequestParams;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.ObfuscatedLoginJavaScriptFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.PasswordVirtualKeyboardImageFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.SegmentIdFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.PasswordVirtualKeyboardOcr;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview.MainViewRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview.PostLoginFormSubmitRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

@AllArgsConstructor
public class CajasurAuthenticationApiClient {

    private static final String BEFORE_AUTH_URL_DOMAIN = "https://portal.cajasur.es";
    private static final String AUTH_PROCESSING_URL_DOMAIN = "https://cajasur.es";

    private final TinkHttpClient tinkHttpClient;
    private final SessionStorage sessionStorage;
    private final PasswordVirtualKeyboardOcr passwordVirtualKeyboardOcr;

    public String callForSegmentId() {
        return Optional.ofNullable(
                        new SegmentIdFetchRequest(BEFORE_AUTH_URL_DOMAIN)
                                .call(tinkHttpClient, sessionStorage))
                .orElseThrow(
                        () ->
                                new ConnectivityException(
                                                ConnectivityErrorDetails.TinkSideErrors
                                                        .TINK_INTERNAL_SERVER_ERROR)
                                        .withInternalMessage("Didn't find a segemnt id"));
    }

    public String callForEncryptObfuscatedLoginJS() {
        return Optional.ofNullable(
                        new ObfuscatedLoginJavaScriptFetchRequest(BEFORE_AUTH_URL_DOMAIN)
                                .call(tinkHttpClient, sessionStorage))
                .orElseThrow(
                        () ->
                                new ConnectivityException(
                                                ConnectivityErrorDetails.TinkSideErrors
                                                        .TINK_INTERNAL_SERVER_ERROR)
                                        .withInternalMessage(
                                                "Didn't find the encrypt obfuscated login JS"));
    }

    public BufferedImage callForPasswordVirtualKeyboardImage() {
        return new PasswordVirtualKeyboardImageFetchRequest(AUTH_PROCESSING_URL_DOMAIN)
                .call(tinkHttpClient, sessionStorage);
    }

    public String callLogin(LoginRequestParams params) {
        return new LoginRequest(AUTH_PROCESSING_URL_DOMAIN, passwordVirtualKeyboardOcr, params)
                .call(tinkHttpClient, sessionStorage);
    }

    public URL submitPostLoginForm(CajasurSessionState sessionState) {
        return new PostLoginFormSubmitRequest(AUTH_PROCESSING_URL_DOMAIN, sessionState)
                .call(tinkHttpClient, sessionStorage);
    }

    public String callForGlobalPositionBody(URL url) {
        return new MainViewRequest(url).call(tinkHttpClient, sessionStorage);
    }
}
