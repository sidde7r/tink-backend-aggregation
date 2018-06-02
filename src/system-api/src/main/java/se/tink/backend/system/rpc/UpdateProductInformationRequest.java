package se.tink.backend.system.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.core.product.ProductPropertyKey;

public class UpdateProductInformationRequest {
    private String userId;
    private UUID productInstanceId;
    private HashMap<ProductPropertyKey, Object> productProperties;

    public UpdateProductInformationRequest() {
    }

    public UpdateProductInformationRequest(String userId, UUID productInstanceId,
            HashMap<ProductPropertyKey, Object> productProperties) {
        this.userId = userId;
        this.productInstanceId = productInstanceId;
        this.productProperties = productProperties;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UUID getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(UUID productInstanceId) {
        this.productInstanceId = productInstanceId;
    }

    public Map<ProductPropertyKey, Object> getProductProperties() {
        return productProperties;
    }

    public void setProductProperties(HashMap<ProductPropertyKey, Object> productProperties) {
        this.productProperties = productProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UpdateProductInformationRequest that = (UpdateProductInformationRequest) o;

        return Objects.equal(this.userId, that.userId) &&
                Objects.equal(this.productInstanceId, that.productInstanceId) &&
                Objects.equal(this.productProperties, that.productProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, productInstanceId, productProperties);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("productInstanceId", productInstanceId)
                .add("productProperties", productProperties)
                .toString();
    }
}
