package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator;

import java.util.Map;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaPartnerAuthenticator implements AutoAuthenticator, MultiFactorAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(NordeaPartnerAuthenticator.class);
    private final SupplementalInformationController supplementalInformationController;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final NordeaPartnerJweHelper jweHelper;

    public NordeaPartnerAuthenticator(
            SupplementalInformationController supplementalInformationController,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            NordeaPartnerJweHelper jweHelper) {
        this.supplementalInformationController = supplementalInformationController;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.jweHelper = jweHelper;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String partnerUid =
                persistentStorage.get(NordeaPartnerConstants.StorageKeys.PARTNER_USER_ID);

        // if partner user Id has not been stored before, request it from Nordea
        if (Strings.isNullOrEmpty(partnerUid)) {
            String token = requestNordeaToken();
            partnerUid =
                    jweHelper
                            .extractUserIdFromToken(token)
                            .orElseThrow(ThirdPartyAppError.AUTHENTICATION_ERROR::exception);
            persistentStorage.put(NordeaPartnerConstants.StorageKeys.PARTNER_USER_ID, partnerUid);
        }

        OAuth2Token accessToken = jweHelper.createAccessToken(partnerUid);
        sessionStorage.put(NordeaPartnerConstants.StorageKeys.TOKEN, accessToken);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        OAuth2Token accessToken =
                sessionStorage
                        .get(NordeaPartnerConstants.StorageKeys.TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (accessToken.hasAccessExpired()) {
            String partnerUid =
                    persistentStorage
                            .get(NordeaPartnerConstants.StorageKeys.PARTNER_USER_ID, String.class)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            accessToken = jweHelper.createAccessToken(partnerUid);
            sessionStorage.put(NordeaPartnerConstants.StorageKeys.TOKEN, accessToken);
        }
    }

    private String requestNordeaToken() throws ThirdPartyAppException {
        try {
            Map<String, String> supplementalInformation =
                    supplementalInformationController.askSupplementalInformation(
                            NordeaPartnerConstants.SupplementalFields.TOKEN);
            String token =
                    supplementalInformation.getOrDefault(
                            NordeaPartnerConstants.SupplementalInfoKeys.TOKEN, null);
            if (Strings.isNullOrEmpty(token)) {
                logger.error("Supplemental Information did not contain 'token'");
                throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
            }
            return token;
        } catch (IllegalStateException e) {
            logger.error("Invalid supplemental Information");
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        } catch (SupplementalInfoException e) {
            logger.error("did not get supplemental Info", e);
            throw ThirdPartyAppError.TIMED_OUT.exception();
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}
