package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.authentication;

import java.security.KeyPair;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.ActionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.MethodType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.ChallengeConfirmationStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.ConfirmChallengeCall;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils.ChallengeUtils;

public class AuthenticationChallengeStep extends ChallengeConfirmationStep {

    public AuthenticationChallengeStep(
            MetroDataAccessorFactory metroDataAccessorFactory,
            ConfirmChallengeCall deviceRegistrationChallengeCall) {
        super(metroDataAccessorFactory, deviceRegistrationChallengeCall);
    }

    @Override
    public ConfirmChallengeRequest requestBody(
            MetroAuthenticationData authenticationData, MetroProcessState processState) {
        KeyPair challengeKeys = authenticationData.getChallengeKeyPair();

        return new ConfirmChallengeRequest(
                new UserIdHeaderEntity(authenticationData.getUserId()),
                AssertionEntity.builder()
                        .action(ActionType.AUTHENTICATION)
                        .assertionId(processState.getAssertionId())
                        .challenge(
                                ChallengeUtils.signDataWithShift(
                                        challengeKeys.getPrivate(),
                                        processState.getChallenge()
                                                + processState.getAssertionId()))
                        .version("v2")
                        .method(MethodType.PIN)
                        .assertionType(AssertionType.AUTHENTICATE)
                        .build());
    }
}
