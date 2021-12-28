package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import java.awt.image.BufferedImage;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.LoginRequestParams;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.ObfuscatedLoginJavaScriptFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.PasswordVirtualKeyboardImageFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.SegmentIdFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.PasswordVirtualKeyboardOcr;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

@AllArgsConstructor
public class CajasurAuthenticationApiClient {

    private final String authenticationUrlDomain;
    private final TinkHttpClient tinkHttpClient;
    private final PasswordVirtualKeyboardOcr passwordVirtualKeyboardOcr;

    public String callForSegmentId() {
        return Optional.ofNullable(
                        new SegmentIdFetchRequest(authenticationUrlDomain).call(tinkHttpClient))
                .orElseThrow(
                        () ->
                                new ConnectivityException(
                                                ConnectivityErrorDetails.TinkSideErrors
                                                        .TINK_INTERNAL_SERVER_ERROR)
                                        .withInternalMessage("Didn't find a segemnt id"));
    }

    public String encryptObfuscatedLogin() {
        return Optional.ofNullable(
                        new ObfuscatedLoginJavaScriptFetchRequest(authenticationUrlDomain)
                                .call(tinkHttpClient))
                .orElseThrow(
                        () ->
                                new ConnectivityException(
                                                ConnectivityErrorDetails.TinkSideErrors
                                                        .TINK_INTERNAL_SERVER_ERROR)
                                        .withInternalMessage(
                                                "Didn't find the encrypt obfuscated login JS"));
    }

    public BufferedImage callForPasswordVirtualKeyboardImage() {
        return new PasswordVirtualKeyboardImageFetchRequest(authenticationUrlDomain)
                .call(tinkHttpClient);
    }

    public String callLogin(LoginRequestParams params) {
        return new LoginRequest(authenticationUrlDomain, passwordVirtualKeyboardOcr, params)
                .call(tinkHttpClient);
    }
}
