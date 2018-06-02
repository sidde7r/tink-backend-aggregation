package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.User;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.libraries.application.GenericApplication;

/**
 * A representation of a request for creating a new product for a certain provider. It holds an application where
 * all necessary information should be stored which is required to create the new product.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductRequest extends CredentialsRequest {

    private GenericApplication application;
    private SignableOperation signableOperation;

    public CreateProductRequest() {

    }

    public CreateProductRequest(User user, Provider provider, Credentials credentials, GenericApplication application,
            SignableOperation signableOperation) {
        super(user, provider, credentials);
        this.application = application;
        this.signableOperation = signableOperation;
    }

    public SignableOperation getSignableOperation() {
        return signableOperation;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.PRODUCT_CREATE;
    }

    public void setSignableOperation(SignableOperation signableOperation) {
        this.signableOperation = signableOperation;
    }

    @Override
    public boolean isManual() {
        return true;
    }

    public GenericApplication getApplication() {
        return application;
    }

    public void setApplication(GenericApplication application) {
        this.application = application;
    }
}
