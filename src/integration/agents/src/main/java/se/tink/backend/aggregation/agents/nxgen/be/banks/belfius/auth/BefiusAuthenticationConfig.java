package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.AutoAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.BelfiusAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.IsDeviceRegisteredStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.ManualAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.PasswordLoginEncryptStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.PasswordLoginInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.PasswordLoginStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.RegisterDeviceFinishStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.RegisterDeviceGetLoginCodeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.RegisterDeviceGetSignCodeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.RegisterDeviceLoginStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.RegisterDeviceSignStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.RegisterDeviceStartStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.SoftLoginFinishStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.SoftLoginGetContactNumberAndChallegeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.SoftLoginInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.SoftLoginStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.UsernameAndPasswordGetStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps.UsernameAndPasswordSaveStep;
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
        return new BelfiusAuthenticationInitStep(belfiusPersistedDataAccessorFactory());
    }

    public IsDeviceRegisteredStep isDeviceRegisteredStep() {
        return new IsDeviceRegisteredStep(apiClient, belfiusPersistedDataAccessorFactory());
    }

    public ManualAuthenticationInitStep manualAuthenticationInitStep() {
        return new ManualAuthenticationInitStep(apiClient);
    }

    public PasswordLoginEncryptStep passwordLoginEncryptStep() {
        return new PasswordLoginEncryptStep(
                apiClient, signer, belfiusPersistedDataAccessorFactory());
    }

    public PasswordLoginInitStep passwordLoginInitStep() {
        return new PasswordLoginInitStep(apiClient);
    }

    public PasswordLoginStep passwordLoginStep() {
        return new PasswordLoginStep(apiClient, sessionStorage);
    }

    public RegisterDeviceFinishStep registerDeviceFinishStep() {
        return new RegisterDeviceFinishStep(apiClient, belfiusPersistedDataAccessorFactory());
    }

    public RegisterDeviceGetLoginCodeStep registerDeviceGetLoginCodeStep() {
        return new RegisterDeviceGetLoginCodeStep(apiClient, belfiusPersistedDataAccessorFactory());
    }

    public RegisterDeviceGetSignCodeStep registerDeviceGetSignCodeStep() {
        return new RegisterDeviceGetSignCodeStep(apiClient);
    }

    public RegisterDeviceLoginStep registerDeviceLoginStep() {
        return new RegisterDeviceLoginStep(apiClient);
    }

    public RegisterDeviceSignStep registerDeviceSignStep() {
        return new RegisterDeviceSignStep(apiClient);
    }

    public RegisterDeviceStartStep registerDeviceStartStep() {
        return new RegisterDeviceStartStep(
                apiClient, signer, belfiusPersistedDataAccessorFactory());
    }

    public SoftLoginFinishStep softLoginFinishStep() {
        return new SoftLoginFinishStep(apiClient);
    }

    public SoftLoginGetContactNumberAndChallegeStep softLoginGetContactNumberAndChallegeStep() {
        return new SoftLoginGetContactNumberAndChallegeStep(
                apiClient, belfiusPersistedDataAccessorFactory());
    }

    public SoftLoginInitStep softLoginInitStep() {
        return new SoftLoginInitStep(apiClient);
    }

    public SoftLoginStep softLoginStep() {
        return new SoftLoginStep(apiClient, signer, belfiusPersistedDataAccessorFactory());
    }

    public UsernameAndPasswordGetStep usernameAndPasswordGetStep() {
        return new UsernameAndPasswordGetStep();
    }

    public UsernameAndPasswordSaveStep usernameAndPasswordSaveStep() {
        return new UsernameAndPasswordSaveStep(belfiusPersistedDataAccessorFactory());
    }

    public AutoAuthenticationInitStep autoAuthenticationInitStep() {
        return new AutoAuthenticationInitStep(
                apiClient, signer, belfiusPersistedDataAccessorFactory());
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

    public BelfiusPersistedDataAccessorFactory belfiusPersistedDataAccessorFactory() {
        return new BelfiusPersistedDataAccessorFactory(objectMapper);
    }
}
