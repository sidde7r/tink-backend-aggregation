package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankErrorHandler.ErrorSource;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PostbankApiClient extends DeutscheBankApiClient {
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
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.CONSENT_CREATION);
            throw hre;
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
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.AUTHORISATION_PASSWORD);
            throw hre;
        }
    }

    public AuthorisationResponse getAuthorisation(URL url, String psuId) {
        try {
            return createRequest(url)
                    .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                    .header(HeaderKeys.PSU_ID, psuId)
                    .get(AuthorisationResponse.class);
        } catch (HttpResponseException hre) {
            PostbankErrorHandler.handleError(hre, ErrorSource.AUTHORISATION_FETCH);
            throw hre;
        }
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
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.AUTHORISATION_OTP);
            throw hre;
        }
    }

    private AuthorisationResponse updateAuthorisation(
            URL url, String psuId, UpdateAuthorisationRequest body) {
        try {
            return createRequest(url)
                    .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                    .header(HeaderKeys.PSU_ID, psuId)
                    .put(AuthorisationResponse.class, body);
        } catch (HttpResponseException hre) {
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.AUTHORISATION_OTP);
            throw hre;
        }
    }
}
