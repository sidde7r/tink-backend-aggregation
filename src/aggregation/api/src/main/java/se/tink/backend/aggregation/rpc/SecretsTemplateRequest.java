package se.tink.backend.aggregation.rpc;

import se.tink.backend.agents.rpc.Provider;

public class SecretsTemplateRequest {
    private Provider provider;
    private boolean includeDescriptions;
    private boolean includeExamples;

    public Provider getProvider() {
        return provider;
    }

    public boolean getIncludeDescriptions() {
        return includeDescriptions;
    }

    public boolean getIncludeExamples() {
        return includeExamples;
    }
}
