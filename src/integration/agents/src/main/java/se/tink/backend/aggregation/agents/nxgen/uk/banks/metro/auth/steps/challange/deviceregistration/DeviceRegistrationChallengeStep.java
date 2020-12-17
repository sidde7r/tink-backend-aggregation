package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.deviceregistration;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.ActionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionData;
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

public class DeviceRegistrationChallengeStep extends ChallengeConfirmationStep {

    public DeviceRegistrationChallengeStep(
            MetroDataAccessorFactory metroDataAccessorFactory,
            ConfirmChallengeCall deviceRegistrationChallengeCall) {
        super(metroDataAccessorFactory, deviceRegistrationChallengeCall);
    }

    public ConfirmChallengeRequest requestBody(
            MetroAuthenticationData authenticationData, MetroProcessState processState) {
        KeyPair challengeKeys = authenticationData.getChallengeKeyPair();

        return new ConfirmChallengeRequest(
                new UserIdHeaderEntity(authenticationData.getUserId()),
                AssertionEntity.builder()
                        .action(ActionType.REGISTRATION)
                        .assertionId(processState.getAssertionId())
                        .challenge(
                                ChallengeUtils.signDataWithShift(
                                        challengeKeys.getPrivate(),
                                        processState.getChallenge()
                                                + processState.getAssertionId()))
                        .version("v2")
                        .method(MethodType.PIN)
                        .publicKey(
                                new AssertionData.PublicKeyEntity(
                                        ChallengeUtils.shiftEcPublicKey(
                                                (ECPublicKey) challengeKeys.getPublic()),
                                        "ecraw"))
                        .assertionType(AssertionType.REGISTER)
                        .build());
    }
}
