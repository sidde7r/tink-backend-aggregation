package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator;

import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants.SensitivePayload;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtAccountsListParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtPasswordFormParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtRSAPublicKeyParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtRsa;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IngAtPasswordAuthenticator implements PasswordAuthenticator {
    private final Credentials credentials;
    private final IngAtApiClient apiClient;
    private final IngAtSessionStorage sessionStorage;

    public IngAtPasswordAuthenticator(
            final Credentials credentials,
            final IngAtApiClient apiClient,
            final IngAtSessionStorage sessionStorage) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private Form fillInPasswordForm(String htmlText, String username, String password) {
        final IngAtRSAPublicKeyParser rsaParser = new IngAtRSAPublicKeyParser(htmlText);
        final Optional<RSAPublicKeySpec> optionalPublicKeySpec = rsaParser.getPublicKeySpec();
        final RSAPublicKeySpec publicKeySpec =
                optionalPublicKeySpec.orElseGet(
                        IngAtRSAPublicKeyParser::getDefaultRSAPublicKeySpec);
        final IngAtRsa rsa = new IngAtRsa(publicKeySpec);
        final Form passwordForm = new IngAtPasswordFormParser(rsaParser.getDocument()).getForm();
        final String encryptedPassword = rsa.encrypt(password);
        return passwordForm
                .rebuilder()
                .put("pinLoginHash", encryptedPassword)
                .put("loginFeedBack:loginFeedBack_body:login", username)
                .put("pin", org.apache.commons.lang3.StringUtils.repeat("X", password.length()))
                .build();
    }

    private static AccountReferenceEntity summaryToAccountReference(
            final IngAtAccountsListParser.AccountSummary summary) {
        final String accountPrefix = IngAtConstants.Url.ACCOUNT_PREFIX.toString();

        return new AccountReferenceEntity(
                summary.getId(),
                summary.getType(),
                summary.getAccountName(),
                accountPrefix + summary.getLink().replaceFirst("./", ""));
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final String response =
                apiClient.initiateWebLogin(IngAtConstants.Url.AUTH_START).getBody(String.class);
        credentials.setSensitivePayload(SensitivePayload.loginResponse, response);
        final Form passwordForm = fillInPasswordForm(response, username, password);
        final HttpResponse loginResponse =
                apiClient.logIn(IngAtConstants.Url.PASSWORD, passwordForm);
        final URL page0 = IngAtApiClient.getLastRedirectUrl(loginResponse);
        final IngAtAccountsListParser parser =
                new IngAtAccountsListParser(loginResponse.getBody(String.class));
        final List<IngAtAccountsListParser.AccountSummary> accountsSummary =
                parser.getAccountsSummary();
        final List<AccountReferenceEntity> accountEntities =
                accountsSummary.stream()
                        .map(IngAtPasswordAuthenticator::summaryToAccountReference)
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
