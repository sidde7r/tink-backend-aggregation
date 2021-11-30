package se.tink.agent.sdk.models.account.builder;

public interface BankReferenceBuildStep<T> {
    T bankReference(String bankReference);

    T bankReference(Object bankReference);
}
