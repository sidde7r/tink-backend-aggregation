package se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.common.aggregationcontroller.v1.enums.CredentialsRequestType;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.product.ProductType;

public class RefreshApplicationRequest extends CredentialsRequest {
    private ProductType productType;
    private UUID applicationId;
    private Map<RefreshApplicationParameterKey, Object> parameters;

    public RefreshApplicationRequest() {}

    public RefreshApplicationRequest(User user, Provider provider, Credentials credentials, ProductType productType,
            UUID applicationId, HashMap<RefreshApplicationParameterKey, Object> parameters) {
        super(user, provider, credentials);
        this.productType = productType;
        this.applicationId = applicationId;
        this.parameters = parameters;
    }

    @Override
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.PRODUCT_REFRESH;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public Map<RefreshApplicationParameterKey, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<RefreshApplicationParameterKey, Object> parameters) {
        this.parameters = parameters;
    }
}
