package se.tink.backend.product.execution.api.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.model.User;

public class ProductInformationRequest {
    private ProductType productType;
    private UUID productInstanceId;
    private User user;

    private Map<FetchProductInformationParameterKey, Object> parameters;

    public ProductInformationRequest() {
    }

    public ProductInformationRequest(User user, ProductType productType, UUID productInstanceId,
            Map<FetchProductInformationParameterKey, Object> parameters) {
        this.user = user;
        this.productType = productType;
        this.productInstanceId = productInstanceId;
        this.parameters = parameters;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public UUID getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(UUID productInstanceId) {
        this.productInstanceId = productInstanceId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<FetchProductInformationParameterKey, Object> getParameters() {
        return parameters;
    }

    public void setParameters(
            Map<FetchProductInformationParameterKey, Object> parameters) {
        this.parameters = parameters;
    }
}
