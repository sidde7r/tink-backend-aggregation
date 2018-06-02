package se.tink.backend.rpc.abnamro;

import io.protostuff.Tag;

public class CustomerValidationRequest {

    @Tag(1)
    private long accountNumber;
    @Tag(2)
    private long cardNumber;

    public long getAccountNumber() {
        return accountNumber;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCardNumber(long cardNumber) {
        this.cardNumber = cardNumber;
    }
}
