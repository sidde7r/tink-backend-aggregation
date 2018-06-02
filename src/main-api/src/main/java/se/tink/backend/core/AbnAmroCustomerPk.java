package se.tink.backend.core;

import java.io.Serializable;
import javax.persistence.IdClass;

@IdClass(AbnAmroCustomerPk.class)
public class AbnAmroCustomerPk implements Serializable {
    private static final long serialVersionUID = 1L;
    private long accountNumber;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (accountNumber ^ (accountNumber >>> 32));
        result = prime * result + (int) (cardNumber ^ (cardNumber >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbnAmroCustomerPk)) {
            return false;
        }
        AbnAmroCustomerPk other = (AbnAmroCustomerPk) obj;
        if (accountNumber != other.accountNumber) {
            return false;
        }
        if (cardNumber != other.cardNumber) {
            return false;
        }
        return true;
    }
}
