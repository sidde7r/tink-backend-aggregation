package se.tink.backend.aggregation.rpc;

public class CreateProductResponse {
    private String externalId;
    
    public CreateProductResponse() {

    }

    public CreateProductResponse(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
