package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankErrorHandler.ErrorSource;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.PsuData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.StartAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.UpdateAuthorisationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto.JwtGenerator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AdditionalInformation;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.charsetguesser.CharsetGuesser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class PostbankApiClient extends DeutscheBankApiClient {
    private JwtGenerator jwtGenerator;

    public PostbankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            DeutscheHeaderValues headerValues,
            DeutscheMarketConfiguration marketConfiguration,
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource) {
        super(
                client,
                persistentStorage,
                headerValues,
                marketConfiguration,
                randomValueGenerator,
                localDateTimeSource);
    }

    public void enrichWithJwtGenerator(JwtGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    public ConsentResponse getConsents(String psuId) {
        ConsentRequest consentRequest =
                ConsentRequest.buildTypicalRecurring(
                        AccessEntity.builder()
                                .allPsd2(AccessType.ALL_ACCOUNTS)
                                .additionalInformation(new AdditionalInformation())
                                .build(),
                        localDateTimeSource.now().toLocalDate().plusDays(89).toString());
        try {
            return createRequestWithServiceMapped(
                            new URL(marketConfiguration.getBaseUrl() + Urls.CONSENT))
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

    public AuthorizationResponse startAuthorization(URL url, String psuId, String password) {
        StartAuthorisationRequest startAuthorisationRequest =
                new StartAuthorisationRequest(new PsuData(jwtGenerator.createJWT(password)));

        try {
            AuthorizationResponse authorisationResponse =
                    createRequest(url)
                            .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                            .header(HeaderKeys.PSU_ID, psuId)
                            .put(AuthorizationResponse.class, startAuthorisationRequest.toData());
            // NZG-297: Logging to observe success/failures depending on special characters
            log.info(
                    "SUCCESS_LOGIN username charset: [{}]  password charset: [{}]",
                    CharsetGuesser.getCharset(psuId),
                    CharsetGuesser.getCharset(password));
            return authorisationResponse;
        } catch (HttpResponseException hre) {
            log.info(
                    "FAILED_LOGIN username charset: [{}]  password charset: [{}]",
                    CharsetGuesser.getCharset(psuId),
                    CharsetGuesser.getCharset(password));
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.AUTHORISATION_PASSWORD);
            throw hre;
        }
    }

    public AuthorizationResponse getAuthorization(URL url, String psuId) {
        try {
            return createRequest(url)
                    .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                    .header(HeaderKeys.PSU_ID, psuId)
                    .get(AuthorizationResponse.class);
        } catch (HttpResponseException hre) {
            PostbankErrorHandler.handleError(hre, ErrorSource.AUTHORISATION_FETCH);
            throw hre;
        }
    }

    public AuthorizationResponse updateAuthorizationForScaMethod(
            URL url, String psuId, String methodId) {
        UpdateAuthorisationRequest updateAuthorisationRequest =
                new UpdateAuthorisationRequest(null, methodId);
        return updateAuthorization(url, psuId, updateAuthorisationRequest);
    }

    public AuthorizationResponse updateAuthorizationForOtp(URL url, String psuId, String otp) {
        try {
            UpdateAuthorisationRequest updateAuthorisationRequest =
                    new UpdateAuthorisationRequest(otp, null);
            return updateAuthorization(url, psuId, updateAuthorisationRequest);
        } catch (HttpResponseException hre) {
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.AUTHORISATION_OTP);
            throw hre;
        }
    }

    private AuthorizationResponse updateAuthorization(
            URL url, String psuId, UpdateAuthorisationRequest body) {
        try {
            return createRequest(url)
                    .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                    .header(HeaderKeys.PSU_ID, psuId)
                    .put(AuthorizationResponse.class, body);
        } catch (HttpResponseException hre) {
            PostbankErrorHandler.handleError(
                    hre, PostbankErrorHandler.ErrorSource.AUTHORISATION_OTP);
            throw hre;
        }
    }
}
