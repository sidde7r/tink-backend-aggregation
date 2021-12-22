package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.builder.ThirdPartyAppResultBuildStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.builder.ThirdPartyAppResultReferenceBuildStep;
import se.tink.agent.sdk.storage.SerializableReference;

public class ThirdPartyAppResultBuilder
        implements ThirdPartyAppResultReferenceBuildStep, ThirdPartyAppResultBuildStep {
    private final ThirdPartyAppStatus status;
    private SerializableReference reference;

    ThirdPartyAppResultBuilder(ThirdPartyAppStatus status) {
        this.status = status;
    }

    @Override
    public ThirdPartyAppResultBuildStep reference(String reference) {
        this.reference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public ThirdPartyAppResultBuildStep reference(Object reference) {
        this.reference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public ThirdPartyAppResultBuildStep noReference() {
        this.reference = null;
        return this;
    }

    @Override
    public ThirdPartyAppResult build() {
        return new ThirdPartyAppResult(this.status, this.reference);
    }
}
