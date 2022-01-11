package se.tink.agent.sdk.steppable_execution.base_step;

import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.storage.Storage;

public interface StepRequestBase {
    User getUser();

    StaticBankCredentials getStaticBankCredentials();

    Storage getStepStorage();

    Storage getAgentStorage();
}
