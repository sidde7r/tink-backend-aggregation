package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.agents.rpc.User;
import se.tink.backend.agents.rpc.Provider;

/**
 * A representation of a request for getting product information for the user. There is not a given credentials at this
 * point for the user since the product information is not bound to specific credential.
 *
 * All necessary parameters for requesting the information from the partner should be stored inside the
 * ProductInformationParameters.
 */
public class ProductInformationRequest extends FakedCredentialsRequest {
    private ProductType productType;
    private UUID productInstanceId;
    private HashMap<FetchProductInformationParameterKey, Object> parameters;

    public ProductInformationRequest() {}

    public ProductInformationRequest(User user, Provider provider, ProductType productType, UUID productInstanceId,
            HashMap<FetchProductInformationParameterKey, Object> parameters) {
        super(user, provider);
        this.productType = productType;
        this.productInstanceId = productInstanceId;
        this.parameters = parameters;
    }

    @JsonIgnore
    @Override
    public boolean isManual() {
        return false;
    }

    @JsonIgnore
    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.PRODUCT_INFORMATION;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public Map<FetchProductInformationParameterKey, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<FetchProductInformationParameterKey, Object> parameters) {
        this.parameters = parameters;
    }

    public UUID getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(UUID productInstanceId) {
        this.productInstanceId = productInstanceId;
    }
}
