package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.PsuData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.StartAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.UpdateAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.utils.PostbankCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.GlobalConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PostbankApiClient extends DeutscheBankApiClient {
    private static final String ERR_BAD_REQUEST = "Bad Request";
    private static final String ERR_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";

    public PostbankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            DeutscheHeaderValues headerValues,
            DeutscheMarketConfiguration marketConfiguration) {
        super(client, persistentStorage, headerValues, marketConfiguration);
    }

    public ConsentResponse getConsents(String psuId) {
        ConsentRequest consentRequest = new ConsentRequest(new GlobalConsentAccessEntity());
        try {
            return createRequest(new URL(marketConfiguration.getBaseUrl() + Urls.CONSENT))
                    .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                    .header(HeaderKeys.PSU_ID, psuId)
                    .type(MediaType.APPLICATION_JSON)
                    .post(ConsentResponse.class, consentRequest);
        } catch (HttpResponseException hre) {
            handleHttpResponseException(
                    hre, ERR_BAD_REQUEST, LoginError.INCORRECT_CREDENTIALS.exception(hre));
            return null;
        }
    }

    public AuthorisationResponse startAuthorisation(URL url, String psuId, String password) {
        PostbankCryptoUtils encryptedPassword = new PostbankCryptoUtils();

        StartAuthorisationRequest startAuthorisationRequest =
                new StartAuthorisationRequest(new PsuData(encryptedPassword.createJWT(password)));

        try {
            return createRequest(url)
                    .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                    .header(HeaderKeys.PSU_ID, psuId)
                    .put(AuthorisationResponse.class, startAuthorisationRequest.toData());
        } catch (HttpResponseException hre) {
            handleHttpResponseException(
                    hre, ERR_CREDENTIALS_INVALID, LoginError.INCORRECT_CREDENTIALS.exception(hre));
            return null;
        }
    }

    public AuthorisationResponse getAuthorisation(URL url, String psuId) {
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .get(AuthorisationResponse.class);
    }

    public AuthorisationResponse updateAuthorisationForScaMethod(
            URL url, String psuId, String methodId) {
        UpdateAuthorisationRequest updateAuthorisationRequest =
                new UpdateAuthorisationRequest(null, methodId);
        return updateAuthorisation(url, psuId, updateAuthorisationRequest);
    }

    public AuthorisationResponse updateAuthorisationForOtp(URL url, String psuId, String otp) {
        try {
            UpdateAuthorisationRequest updateAuthorisationRequest =
                    new UpdateAuthorisationRequest(otp, null);
            return updateAuthorisation(url, psuId, updateAuthorisationRequest);
        } catch (HttpResponseException hre) {
            handleHttpResponseException(
                    hre,
                    ERR_CREDENTIALS_INVALID,
                    LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(hre));
            return null;
        }
    }

    private AuthorisationResponse updateAuthorisation(
            URL url, String psuId, UpdateAuthorisationRequest body) {
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .put(AuthorisationResponse.class, body);
    }

    private void handleHttpResponseException(
            HttpResponseException hre, String expectedErrorCode, RuntimeException toThrow) {
        try {
            ErrorResponse errorResponse = hre.getResponse().getBody(ErrorResponse.class);
            if (errorResponse.getTppMessages() == null
                    || errorResponse.getTppMessages().size() == 0) {
                throw hre;
            }

            errorResponse.getTppMessages().stream()
                    .filter(x -> x.isError() && expectedErrorCode.equalsIgnoreCase(x.getCode()))
                    .findFirst()
                    .orElseThrow(() -> hre);
            throw toThrow;
        } catch (HttpClientException hce) {
            // Could not parse it as ErrorResponse, continue with exception
            throw hre;
        }
    }
}
