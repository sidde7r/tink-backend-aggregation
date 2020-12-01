package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.AutoAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.BelfiusAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.IsDeviceRegisteredStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.ManualAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.PasswordLoginEncryptStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.PasswordLoginInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.PasswordLoginStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceFinishStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceGetLoginCodeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceGetSignCodeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceLoginStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceSignStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceStartStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.SoftLoginFinishStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.SoftLoginGetContactNumberAndChallegeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.SoftLoginInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.SoftLoginStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.UsernameAndPasswordGetStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.UsernameAndPasswordSaveStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
public class BefiusAuthenticationConfig {

    private final AgentPlatformBelfiusApiClient apiClient;
    private final BelfiusSessionStorage sessionStorage;
    private final BelfiusSignatureCreator signer;
    private final ObjectMapper objectMapper;

    public BelfiusAuthenticationInitStep belfiusAuthenticationInitStep() {
        return new BelfiusAuthenticationInitStep(belfiusDataAccessorFactory());
    }

    public IsDeviceRegisteredStep isDeviceRegisteredStep() {
        return new IsDeviceRegisteredStep(apiClient, belfiusDataAccessorFactory());
    }

    public ManualAuthenticationInitStep manualAuthenticationInitStep() {
        return new ManualAuthenticationInitStep(apiClient);
    }

    public PasswordLoginEncryptStep passwordLoginEncryptStep() {
        return new PasswordLoginEncryptStep(apiClient, signer, belfiusDataAccessorFactory());
    }

    public PasswordLoginInitStep passwordLoginInitStep() {
        return new PasswordLoginInitStep(apiClient, belfiusDataAccessorFactory());
    }

    public PasswordLoginStep passwordLoginStep() {
        return new PasswordLoginStep(apiClient, sessionStorage, belfiusDataAccessorFactory());
    }

    public RegisterDeviceFinishStep registerDeviceFinishStep() {
        return new RegisterDeviceFinishStep(apiClient, belfiusDataAccessorFactory());
    }

    public RegisterDeviceGetLoginCodeStep registerDeviceGetLoginCodeStep() {
        return new RegisterDeviceGetLoginCodeStep(apiClient, belfiusDataAccessorFactory());
    }

    public RegisterDeviceGetSignCodeStep registerDeviceGetSignCodeStep() {
        return new RegisterDeviceGetSignCodeStep(apiClient, belfiusDataAccessorFactory());
    }

    public RegisterDeviceLoginStep registerDeviceLoginStep() {
        return new RegisterDeviceLoginStep(apiClient, belfiusDataAccessorFactory());
    }

    public RegisterDeviceSignStep registerDeviceSignStep() {
        return new RegisterDeviceSignStep(apiClient, belfiusDataAccessorFactory());
    }

    public RegisterDeviceStartStep registerDeviceStartStep() {
        return new RegisterDeviceStartStep(apiClient, signer, belfiusDataAccessorFactory());
    }

    public SoftLoginFinishStep softLoginFinishStep() {
        return new SoftLoginFinishStep(apiClient, belfiusDataAccessorFactory());
    }

    public SoftLoginGetContactNumberAndChallegeStep softLoginGetContactNumberAndChallegeStep() {
        return new SoftLoginGetContactNumberAndChallegeStep(
                apiClient, belfiusDataAccessorFactory());
    }

    public SoftLoginInitStep softLoginInitStep() {
        return new SoftLoginInitStep(apiClient, belfiusDataAccessorFactory());
    }

    public SoftLoginStep softLoginStep() {
        return new SoftLoginStep(apiClient, signer, belfiusDataAccessorFactory());
    }

    public UsernameAndPasswordGetStep usernameAndPasswordGetStep() {
        return new UsernameAndPasswordGetStep();
    }

    public UsernameAndPasswordSaveStep usernameAndPasswordSaveStep() {
        return new UsernameAndPasswordSaveStep(belfiusDataAccessorFactory());
    }

    public AutoAuthenticationInitStep autoAuthenticationInitStep() {
        return new AutoAuthenticationInitStep(apiClient, signer, belfiusDataAccessorFactory());
    }

    public AgentAuthenticationProcess belfiusAuthProcess() {
        return new AgentAbstractMultiStepsAuthenticationProcess() {

            @Override
            public void registerSteps() {
                addStep(isDeviceRegisteredStep());
                addStep(manualAuthenticationInitStep());
                addStep(passwordLoginEncryptStep());
                addStep(passwordLoginInitStep());
                addStep(passwordLoginStep());
                addStep(registerDeviceFinishStep());
                addStep(registerDeviceGetLoginCodeStep());
                addStep(registerDeviceGetSignCodeStep());
                addStep(registerDeviceLoginStep());
                addStep(registerDeviceSignStep());
                addStep(registerDeviceStartStep());
                addStep(softLoginFinishStep());
                addStep(softLoginGetContactNumberAndChallegeStep());
                addStep(softLoginInitStep());
                addStep(softLoginStep());
                addStep(usernameAndPasswordGetStep());
                addStep(usernameAndPasswordSaveStep());
                addStep(autoAuthenticationInitStep());
            }

            @Override
            public AgentAuthenticationProcessStep getStartStep() {
                return belfiusAuthenticationInitStep();
            }
        };
    }

    public BelfiusDataAccessorFactory belfiusDataAccessorFactory() {
        return new BelfiusDataAccessorFactory(objectMapper);
    }
}
