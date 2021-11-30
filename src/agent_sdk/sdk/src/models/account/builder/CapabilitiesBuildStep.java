package se.tink.agent.sdk.models.account.builder;

import se.tink.agent.sdk.models.account.AccountCapabilities;

public interface CapabilitiesBuildStep<T> {
    T capabilities(AccountCapabilities capabilities);

    T unknownCapabilities();
}
