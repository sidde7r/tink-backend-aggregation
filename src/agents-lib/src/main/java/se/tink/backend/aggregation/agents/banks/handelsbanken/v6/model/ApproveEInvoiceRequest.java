package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApproveEInvoiceRequest {
    private List<String> approvalIds;

    public List<String> getApprovalIds() {
        return approvalIds;
    }

    public void setApprovalIds(List<String> approvalIds) {
        this.approvalIds = approvalIds;
    }

    public static ApproveEInvoiceRequest create(List<String> approvalIds) {
        ApproveEInvoiceRequest request = new ApproveEInvoiceRequest();
        request.setApprovalIds(approvalIds);
        return request;
    }
}
