package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder;

public interface InterestStep<T> {

    T withInterestRate(double interestRate);
}
