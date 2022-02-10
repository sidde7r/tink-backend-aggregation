package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class ArgentaAuthenticator implements OAuth2Authenticator {

    private static final Pattern IBAN_PATTERN = Pattern.compile("BE[0-9]{14}");

    private final Credentials credentials;
    private final ArgentaApiClient apiClient;
    private final ArgentaStorage argentaStorage;

    @Override
    public URL buildAuthorizeUrl(String state) {
        List<IbanEntity> ibans =
                Stream.of(credentials.getField(CredentialKeys.IBAN).split(","))
                        .map(String::trim)
                        .map(IbanEntity::new)
                        .filter(this::verifyIban)
                        .collect(Collectors.toList());
        ConsentResponse consentResponse;
        try {
            consentResponse = apiClient.getConsent(ibans);
        } catch (HttpResponseException hre) {
            if (isFormatError(hre.getResponse())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw hre;
        }

        argentaStorage.storeConsentId(consentResponse.getConsentId());
        return apiClient.buildAuthorizeUrl(state, consentResponse.getConsentId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.exchangeAuthorizationCode(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        return apiClient.exchangeRefreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        argentaStorage.storeAccessToken(accessToken);
    }

    private boolean verifyIban(IbanEntity ibanEntity) {
        if (!IBAN_PATTERN.matcher(ibanEntity.getIban()).matches()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    new LocalizableKey(
                            "Please enter a correct IBAN. It must start with ‘BE’ in capital letters."));
        }
        return true;
    }

    private boolean isFormatError(HttpResponse response) {
        return response.getStatus() == 400
                && response.getBody(String.class).contains("FORMAT_ERROR");
    }
}
