package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public interface SupplementalInformationHelper {

    String waitForLoginInput() throws SupplementalInfoException;

    String waitForAddBeneficiaryInput() throws SupplementalInfoException;

    String waitForOtpInput() throws SupplementalInfoException;

    String waitForLoginChallengeResponse(String challenge) throws SupplementalInfoException;

    String waitForSignCodeChallengeResponse(String challenge) throws SupplementalInfoException;

    String waitForSignForBeneficiaryChallengeResponse(String challenge)
            throws SupplementalInfoException;

    String waitForTwoStepSignForBeneficiaryChallengeResponse(
            String challenge, String extraChallenge) throws SupplementalInfoException;

    String waitForSignForTransferChallengeResponse(String challenge)
            throws SupplementalInfoException;

    String waitForTwoStepSignForTransferChallengeResponse(String challenge, String extraChallenge)
            throws SupplementalInfoException;

    void waitAndShowLoginDescription(String description) throws SupplementalInfoException;

    Map<String, String> askSupplementalInformation(Field... fields)
            throws SupplementalInfoException;

    Optional<Map<String, String>> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit);

    void openThirdPartyApp(ThirdPartyAppAuthenticationPayload payload);
}
