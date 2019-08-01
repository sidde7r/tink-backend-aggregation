package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalPaymentCoverageReportEntity {
    @JsonProperty("request")
    private PaymentCoverageRequestResourceEntity request = null;

    @JsonProperty("result")
    private Boolean result = null;

    @JsonProperty("_links")
    private PaymentCoverageReportLinksEntity links = null;

    public PaymentCoverageRequestResourceEntity getRequest() {
        return request;
    }

    public void setRequest(PaymentCoverageRequestResourceEntity request) {
        this.request = request;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public PaymentCoverageReportLinksEntity getLinks() {
        return links;
    }

    public void setLinks(PaymentCoverageReportLinksEntity links) {
        this.links = links;
    }
}
