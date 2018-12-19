package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtAccountsListParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtPasswordFormParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtRSAPublicKeyParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtRsa;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngAtPasswordAuthenticator implements PasswordAuthenticator {
    private final IngAtApiClient apiClient;
    private final IngAtSessionStorage sessionStorage;

    public IngAtPasswordAuthenticator(
            final IngAtApiClient apiClient, final IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private Form fillInPasswordForm(String htmlText, String username, String password) {
        final IngAtRSAPublicKeyParser rsaParser = new IngAtRSAPublicKeyParser(htmlText);
        final Optional<RSAPublicKeySpec> optionalPublicKeySpec = rsaParser.getPublicKeySpec();
        final RSAPublicKeySpec publicKeySpec =
                optionalPublicKeySpec.isPresent()
                        ? optionalPublicKeySpec.get()
                        : IngAtRSAPublicKeyParser.getDefaultRSAPublicKeySpec();
        final IngAtRsa rsa = new IngAtRsa(publicKeySpec);
        final Form passwordForm = new IngAtPasswordFormParser(rsaParser.getDocument()).getForm();
        final String encryptedPassword = rsa.encrypt(password);
        return new Form.Builder(passwordForm)
                .put("pinLoginHash", encryptedPassword)
                .put("loginFeedBack:loginFeedBack_body:login", username)
                .put("pin", org.apache.commons.lang3.StringUtils.repeat("X", password.length()))
                .build();
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final HttpResponse response = apiClient.initiateWebLogin(IngAtConstants.Url.AUTH_START);
        final Form passwordForm =
                fillInPasswordForm(response.getBody(String.class), username, password);
        final HttpResponse loginResponse =
                apiClient.logIn(IngAtConstants.Url.PASSWORD, passwordForm);
        final URL page0 = IngAtApiClient.getLastRedirectUrl(loginResponse);
        final String accountPrefix = IngAtConstants.Url.ACCOUNT_PREFIX.toString();
        final IngAtAccountsListParser parser =
                new IngAtAccountsListParser(loginResponse.getBody(String.class));
        final List<IngAtAccountsListParser.AccountSummary> accountsSummary =
                parser.getAccountsSummary();
        final List<AccountReferenceEntity> accountEntities =
                accountsSummary
                        .stream()
                        .map(
                                a ->
                                        new AccountReferenceEntity(
                                                a.getId(),
                                                a.getType(),
                                                a.getAccountName(),
                                                accountPrefix + a.getLink().replaceFirst("./", "")))
                        .collect(Collectors.toList());
        final WebLoginResponse webLoginResponse =
                new WebLoginResponse(
                        username,
                        parser.getAccountHolder().orElse(null),
                        page0.toString(),
                        accountEntities);
        sessionStorage.setWebLoginResponse(webLoginResponse);
    }
}
