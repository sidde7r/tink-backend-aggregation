package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "credentials_aggregation")
public class AggregationCredentials {
    @Id
    private String credentialsId;
    @Type(type = "text")
    private String encryptedFields;
    @Type(type = "text")
    private String encryptedPayload;
    @Type(type = "text")
    private String encryptedPrivateKey;
    @Type(type = "text")
    protected String secretKey;

    public AggregationCredentials() {
        // Ok.
    }

    public AggregationCredentials(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getEncryptedFields() {
        return encryptedFields;
    }

    public String getEncryptedPayload() {
        return encryptedPayload;
    }
    
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setEncryptedFields(String encryptedFields) {
        this.encryptedFields = encryptedFields;
    }

    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }
    
    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
