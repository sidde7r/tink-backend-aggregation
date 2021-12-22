package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.builder;

public interface ThirdPartyAppResultReferenceBuildStep {
    ThirdPartyAppResultBuildStep reference(String reference);

    ThirdPartyAppResultBuildStep reference(Object reference);

    ThirdPartyAppResultBuildStep noReference();
}
