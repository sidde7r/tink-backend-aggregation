package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

public interface CardNumberStep<T> {

    CardAliasStep<T> withCardNumber(String cardNumber);
}
