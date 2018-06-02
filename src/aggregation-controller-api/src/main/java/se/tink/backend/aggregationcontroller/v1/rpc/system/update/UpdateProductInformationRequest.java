package se.tink.backend.aggregationcontroller.v1.rpc.system.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.UUID;
import se.tink.backend.core.product.ProductPropertyKey;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductInformationRequest {
    private String userId;
    private UUID productInstanceId;
    private HashMap<ProductPropertyKey, Object> productProperties;

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

    public HashMap<ProductPropertyKey, Object> getProductProperties() {
        return productProperties;
    }

    public void setProductProperties(
            HashMap<ProductPropertyKey, Object> productProperties) {
        this.productProperties = productProperties;
    }
}
