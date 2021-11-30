package se.tink.agent.sdk.models.account.builder;

import se.tink.agent.sdk.models.account.AccountInterestRate;

public interface InterestRateBuildStep<T> {
    T interestRate(AccountInterestRate interestRate);
}
