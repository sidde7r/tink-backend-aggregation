package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EBankingUserId;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.AuthenticationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.EbankingUsersResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.UserInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;

/**
 * This is a legacy auto authentication step that supports "the old flow". Thanks to that the users
 * that have still active session can continue to do bg refresh without reauthentication.
 *
 * <p>In case of any error the authentication has to be done using new flow
 */
@RequiredArgsConstructor
public class LegacyAutoAuthStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyAutoAuthStep.class);

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());

        FortisAuthData authData = authDataAccessor.get();

        final String password = authData.getLegacyAuthData().getPassword();
        final String muid = authData.getLegacyAuthData().getMuid();

        if (Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(muid)) {
            // should not happen if migrated properly
            throw SessionError.SESSION_EXPIRED.exception();
        }

        final String authenticatorFactorId = authData.getUsername();
        final String smid = authData.getClientNumber();
        final String agreementId = authData.getLegacyAuthData().getAgreementId();
        final String deviceFingerprint = authData.getLegacyAuthData().getDeviceFingerprint();

        EbankingUsersResponse ebankingUsersResponse =
                apiClient.getEBankingUsers(authenticatorFactorId, smid);

        if (ebankingUsersResponse.isError()) {
            return clearLegacyAuthAndFail(authDataAccessor, authData);
        }

        if (ebankingUsersResponse.getValue() != null
                && ebankingUsersResponse.getValue().getEBankingUsers().size() != 0) {
            Optional<EBankingUserId> eBankingUserId =
                    Optional.ofNullable(
                            ebankingUsersResponse
                                    .getValue()
                                    .getEBankingUsers()
                                    .get(0)
                                    .getEBankingUser()
                                    .getEBankingUserId());

            if (!eBankingUserId.isPresent()) {
                return clearLegacyAuthAndFail(authDataAccessor, authData);
            }

            AuthenticationProcessResponse authenticationProcessResponse =
                    apiClient.createAuthenticationProcess(eBankingUserId.get());

            if (authenticationProcessResponse.isError()) {
                return clearLegacyAuthAndFail(authDataAccessor, authData);
            }

            final String authenticationProcessId =
                    authenticationProcessResponse.getValue().getAuthenticationProcessId();

            final ChallengeResponse challengeResponse =
                    apiClient.fetchChallenges(authenticationProcessId);

            String challenge = getChallenge(challengeResponse);

            final String calculateChallenge =
                    FortisUtils.calculateChallenge(
                            muid,
                            password,
                            agreementId,
                            challenge,
                            authenticationProcessResponse.getValue().getAuthenticationProcessId());

            boolean challengesSentSuccessfully =
                    sendChallenges(
                            authenticationProcessId,
                            agreementId,
                            authenticatorFactorId,
                            smid,
                            challenge,
                            calculateChallenge,
                            deviceFingerprint);

            if (!challengesSentSuccessfully) {
                return clearLegacyAuthAndFail(authDataAccessor, authData);
            }

            UserInfoResponse userInfoResponse = apiClient.getUserInfo();

            boolean muidValid = validateMuid(userInfoResponse);

            if (!muidValid) {
                return clearLegacyAuthAndFail(authDataAccessor, authData);
            }

            authData.getLegacyAuthData()
                    .setMuid(userInfoResponse.getValue().getUserData().getMuid());
        } else {
            return clearLegacyAuthAndFail(authDataAccessor, authData);
        }

        return new AgentSucceededAuthenticationResult(authDataAccessor.store(authData));
    }

    private String getChallenge(ChallengeResponse challengeResponse) {
        challengeResponse.getBusinessMessageBulk().checkError();

        List<String> challenges = challengeResponse.getValue().getChallenges();

        if (challenges.size() > 1) {
            LOG.warn(
                    "tag={} Multiple challenges: {}",
                    FortisConstants.LoggingTag.MULTIPLE_CHALLENGES,
                    challenges);
        }

        return challenges.get(0);
    }

    private boolean validateMuid(UserInfoResponse userInfoResponse) throws AuthorizationException {
        return !Strings.isNullOrEmpty(userInfoResponse.getValue().getUserData().getMuid());
    }

    private boolean sendChallenges(
            String authenticationProcessId,
            String agreementId,
            String cardNumber,
            String smid,
            String challenge,
            String calculateChallenge,
            String deviceFingerprint)
            throws LoginException, AuthorizationException {

        String response;
        try {
            response =
                    apiClient.authenticationRequest(
                            authenticationProcessId,
                            agreementId,
                            mask(cardNumber),
                            smid,
                            challenge,
                            calculateChallenge,
                            deviceFingerprint);

        } catch (Exception e) {
            return false;
        }

        return Strings.isNullOrEmpty(response)
                || !response.contains(FortisConstants.ErrorCode.ERROR_CODE);
    }

    private String mask(String cardNumber) {
        StringBuilder cardString = new StringBuilder(cardNumber);
        for (int index = 6; index < 13; index++) {
            cardString.setCharAt(index, 'X');
        }
        return String.valueOf(cardString);
    }

    private AgentFailedAuthenticationResult clearLegacyAuthAndFail(
            FortisAuthDataAccessor authDataAccessor, FortisAuthData authData) {
        authData.clearLegacyAuthData();
        return new AgentFailedAuthenticationResult(
                new SessionExpiredError(), authDataAccessor.store(authData));
    }
}
