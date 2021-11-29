package se.tink.agent.sdk.models.account.builder;

public interface CreditCardBuildStep<T> {
    T cardNumber(String value);
}
