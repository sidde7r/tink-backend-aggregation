package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.InvalidPinResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SelectAgreementRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SdcAutoAuthenticator implements AutoAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SdcApiClient bankClient;
    private final SdcSessionStorage sessionStorage;
    private final SdcConfiguration agentConfiguration;
    private final Credentials credentials;
    private final SdcPersistentStorage persistentStorage;

    public SdcAutoAuthenticator(
            SdcApiClient bankClient,
            SdcSessionStorage sessionStorage,
            SdcConfiguration agentConfiguration,
            Credentials credentials,
            SdcPersistentStorage persistentStorage) {

        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
        this.agentConfiguration = agentConfiguration;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        try {
            String username = this.credentials.getField(Field.Key.USERNAME);
            String password = this.credentials.getField(Field.Key.PASSWORD);

            // setup device to handle the device token
            ChallengeResponse challenge = this.bankClient.getChallenge();
            SdcDevice device = new SdcDevice(this.persistentStorage);

            // if the device is not pinned force new device pinning
            if (device.needsPinning()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            AgreementsResponse agreementsResponse = this.bankClient.pinLogon(username, password);
            SessionStorageAgreements agreements = agreementsResponse.toSessionStorageAgreements();
            if (agreements.isEmpty()) {
                logger.warn(
                        "tag={} User was able to login, but has no agreements?",
                        SdcConstants.Session.LOGIN);
                throw new IllegalStateException("No agreement found");
            }

            // enable app (bankClient) using a device token
            this.bankClient.setDeviceToken(new DeviceToken(challenge, device));

            // if we get a signing needed response we need to re-sign the device pinning
            if (signingNeeded(agreements.get(0))) {
                this.persistentStorage.removeSignedDeviceId();
                throw SessionError.SESSION_EXPIRED.exception();
            }
            this.sessionStorage.setAgreements(agreements);

        } catch (HttpResponseException e) {
            // sdc responds with internal server error when bad credentials

            Optional<InvalidPinResponse> invalidPin = InvalidPinResponse.from(e);
            if (invalidPin.isPresent()) {
                this.persistentStorage.removeSignedDeviceId();
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
            if (SdcConstants.Authentication.isInternalError(e)) {
                // errorMessage is null safe
                String errorMessage =
                        Optional.ofNullable(
                                        e.getResponse()
                                                .getHeaders()
                                                .getFirst(SdcConstants.Headers.X_SDC_ERROR_MESSAGE))
                                .orElse("");
                if (this.agentConfiguration.isLoginError(errorMessage)) {
                    logger.info(errorMessage, e);

                    this.persistentStorage.removeSignedDeviceId();
                    throw SessionError.SESSION_EXPIRED.exception(e);
                }
            }

            throw e;
        }
    }

    /*
       Check if device token is needed by backend or if session is expired. Called during logon.
    */
    private boolean signingNeeded(SessionStorageAgreement agreement) {
        HttpResponse response =
                this.bankClient.internalSelectAgreement(
                        new SelectAgreementRequest()
                                .setUserNumber(agreement.getUserNumber())
                                .setAgreementNumber(agreement.getAgreementId()));

        return signingNeeded(response);
    }

    // header field action code indicates if we need to create/renew our device token
    private boolean signingNeeded(HttpResponse response) {
        MultivaluedMap<String, String> headers = response.getHeaders();
        return headers.containsKey(SdcConstants.Headers.X_SDC_ACTION_CODE)
                && headers.get(SdcConstants.Headers.X_SDC_ACTION_CODE).stream()
                        .filter(headerValue -> !Strings.isNullOrEmpty(headerValue))
                        .map(String::toUpperCase)
                        .anyMatch(SdcConstants.Headers.ACTION_CODE_SIGNING_NEEDED::contains);
    }
}
