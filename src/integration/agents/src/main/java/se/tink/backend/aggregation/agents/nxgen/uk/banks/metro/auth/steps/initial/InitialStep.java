package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.initial;

import java.security.KeyPair;
import java.util.UUID;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication.AutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.getcredentials.CredentialsGetStep;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
public class InitialStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private final MetroDataAccessorFactory metroDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();
        if (authenticationData.isAlreadyRegistered()) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            AutoAuthenticationStep.class.getSimpleName()),
                    request.getAuthenticationProcessState(),
                    request.getAuthenticationPersistedData());
        }
        KeyPair rsaKeyPair = RSA.generateKeyPair(2048);
        KeyPair challengeSignECKeyPair = EllipticCurve.generateKeyPair(256);
        KeyPair requestSignatureECKeyPair = EllipticCurve.generateKeyPair("prime256v1");
        String internalDeviceId = UUID.randomUUID().toString().toUpperCase();
        authenticationData
                .setChallengeSignESKeyPair(challengeSignECKeyPair)
                .setRsaKeyPair(rsaKeyPair)
                .setSignatureESKeyPair(requestSignatureECKeyPair)
                .setInternalDeviceId(internalDeviceId);
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        CredentialsGetStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                persistedDataAccessor.storeAuthenticationData(authenticationData));
    }
}
