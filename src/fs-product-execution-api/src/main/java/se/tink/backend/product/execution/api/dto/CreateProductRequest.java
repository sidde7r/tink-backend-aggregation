package se.tink.backend.product.execution.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.product.execution.model.User;
import se.tink.libraries.application.GenericApplication;

/**
 * A representation of a request for creating a new product for a certain provider. It holds an application where
 * all necessary information should be stored which is required to create the new product.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductRequest {

    private User user;
    private GenericApplication application;
    private SignableOperation signableOperation;
    private Credentials credentials; //TODO: we need this to get app signed now, in the future it should be removed when app logic is adapted.

    public CreateProductRequest() {

    }

    public CreateProductRequest(User user, GenericApplication application, SignableOperation signableOperation,
            Credentials credentials) {
        this.user = user;
        this.application = application;
        this.signableOperation = signableOperation;
        this.credentials = credentials;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GenericApplication getApplication() {
        return application;
    }

    public void setApplication(GenericApplication application) {
        this.application = application;
    }

    public SignableOperation getSignableOperation() {
        return signableOperation;
    }

    public void setSignableOperation(SignableOperation signableOperation) {
        this.signableOperation = signableOperation;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
