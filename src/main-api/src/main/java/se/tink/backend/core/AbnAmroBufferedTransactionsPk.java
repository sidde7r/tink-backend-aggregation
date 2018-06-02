package se.tink.backend.core;

import com.google.common.base.Objects;
import java.io.Serializable;
import javax.persistence.IdClass;

@IdClass(AbnAmroBufferedTransactionsPk.class)
public class AbnAmroBufferedTransactionsPk implements Serializable {

    private static final long serialVersionUID = 1L;
    private String credentialsId;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbnAmroBufferedTransactionsPk that = (AbnAmroBufferedTransactionsPk) o;

        return Objects.equal(this.id, that.id) && Objects.equal(this.credentialsId, that.credentialsId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, credentialsId);
    }
}
