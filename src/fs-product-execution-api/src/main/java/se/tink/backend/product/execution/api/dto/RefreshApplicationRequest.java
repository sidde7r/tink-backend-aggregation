package se.tink.backend.product.execution.api.dto;

import java.util.Map;
import java.util.UUID;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.model.RefreshApplicationParameterKey;
import se.tink.backend.product.execution.model.User;

public class RefreshApplicationRequest {
    private ProductType productType;
    private UUID applicationId;
    private User user;
    private Map<RefreshApplicationParameterKey, Object> parameters;

    public RefreshApplicationRequest() {
    }

    public RefreshApplicationRequest(
            User user,
            ProductType productType,
            UUID applicationId,
            Map<RefreshApplicationParameterKey, Object> parameters) {
        this.user = user;
        this.productType = productType;
        this.applicationId = applicationId;
        this.parameters = parameters;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<RefreshApplicationParameterKey, Object> getParameters() {
        return parameters;
    }

    public void setParameters(
            Map<RefreshApplicationParameterKey, Object> parameters) {
        this.parameters = parameters;
    }
}
