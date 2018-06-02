package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageSignatureResponse {

    // The id of the signature which can be used to get the status of the signature.
    @JsonProperty("signaturId")
    private String signatureId;

    // The URL where the user should make the actual signing.
    @JsonProperty("signeringsUrl")
    private String signUrl;

    public String getSignatureId() {
        return signatureId;
    }

    public void setSignatureId(String signatureId) {
        this.signatureId = signatureId;
    }

    public String getSignUrl() {
        return signUrl;
    }

    public void setSignUrl(String signUrl) {
        this.signUrl = signUrl;
    }
}
