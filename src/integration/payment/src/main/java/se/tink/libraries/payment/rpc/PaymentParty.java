package se.tink.libraries.payment.rpc;

import lombok.RequiredArgsConstructor;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@RequiredArgsConstructor
public abstract class PaymentParty {
    private final AccountIdentifier accountIdentifier;

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public AccountIdentifierType getAccountIdentifierType() {
        return accountIdentifier.getType();
    }

    public <T> T getAccountIdentifier(Class<T> klass) {
        try {
            return klass.cast(accountIdentifier);
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "Could not find account identifier of expected type! Expected: "
                            + klass.getSimpleName());
        }
    }

    /**
     * @deprecated use {@link #getAccountIdentifier(Class)} or {@link #getAccountIdentifier()}
     *     instead.
     *     <p>This method can return more than just account number, for example IbanIdentifier with
     *     filled bic would return 'DEUTPLP/PL1234567890'.
     */
    @Deprecated
    public String getAccountNumber() {
        return accountIdentifier.getIdentifier();
    }
}
