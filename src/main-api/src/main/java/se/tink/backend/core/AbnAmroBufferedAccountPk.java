package se.tink.backend.core;

import java.io.Serializable;
import javax.persistence.IdClass;

@IdClass(AbnAmroBufferedAccountPk.class)
public class AbnAmroBufferedAccountPk implements Serializable {
    private static final long serialVersionUID = 1L;
    private long accountNumber;
    private String credentialsId;

    public AbnAmroBufferedAccountPk() {
    }

    public AbnAmroBufferedAccountPk(String credentialsId, long accountNumber) {
        this.credentialsId = credentialsId;
        this.accountNumber = accountNumber;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (accountNumber ^ (accountNumber >>> 32));
        result = prime * result + ((credentialsId == null) ? 0 : credentialsId.hashCode());
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
        if (!(obj instanceof AbnAmroBufferedAccountPk)) {
            return false;
        }
        AbnAmroBufferedAccountPk other = (AbnAmroBufferedAccountPk) obj;
        if (accountNumber != other.accountNumber) {
            return false;
        }
        if (credentialsId == null) {
            if (other.credentialsId != null) {
                return false;
            }
        } else if (!credentialsId.equals(other.credentialsId)) {
            return false;
        }
        return true;
    }
}
