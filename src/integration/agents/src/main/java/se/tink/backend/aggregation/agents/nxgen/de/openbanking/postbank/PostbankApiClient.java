package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.StartAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.UpdateAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.utils.PostbankCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class PostbankApiClient extends DeutscheBankApiClient {

    private final TinkHttpClient apiClient;

    public PostbankApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            DeutscheBankConfiguration configuration) {
        super(client, sessionStorage, configuration);
        this.apiClient = client;
    }

    public ConsentResponse getConsents(String iban, String psuId) {
        ConsentBaseRequest consentBaseRequest = new ConsentBaseRequest(iban);
        DeutscheBankConfiguration configuration = getConfiguration();

        return apiClient
                .request(new URL(configuration.getBaseUrl() + Urls.CONSENT))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, configuration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentResponse.class, consentBaseRequest);
    }

    public AuthorisationResponse startAuthorisation(URL url, String psuId, String password) {
        PostbankCryptoUtils encryptedPassword = new PostbankCryptoUtils();

        StartAuthorisationRequest startAuthorisationRequest =
                new StartAuthorisationRequest(
                        new PsuDataEntity(encryptedPassword.createJWT(password)));
        DeutscheBankConfiguration configuration = getConfiguration();

        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, configuration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .put(AuthorisationResponse.class, startAuthorisationRequest.toData());
    }

    public AuthorisationResponse getAuthorisation(URL url, String psuId) {
        DeutscheBankConfiguration configuration = getConfiguration();
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, configuration.getPsuIdType())
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
        UpdateAuthorisationRequest updateAuthorisationRequest =
                new UpdateAuthorisationRequest(otp, null);
        return updateAuthorisation(url, psuId, updateAuthorisationRequest);
    }

    private AuthorisationResponse updateAuthorisation(
            URL url, String psuId, UpdateAuthorisationRequest body) {
        DeutscheBankConfiguration configuration = getConfiguration();

        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, configuration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .put(AuthorisationResponse.class, body);
    }
}
