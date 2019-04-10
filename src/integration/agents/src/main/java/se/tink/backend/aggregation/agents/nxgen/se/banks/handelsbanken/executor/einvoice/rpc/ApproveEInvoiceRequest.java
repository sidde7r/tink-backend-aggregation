package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApproveEInvoiceRequest {
    private List<String> approvalIds;

    public static ApproveEInvoiceRequest create(String approvalId) {
        ApproveEInvoiceRequest request = new ApproveEInvoiceRequest();
        request.approvalIds = new ArrayList<>();
        request.approvalIds.add(approvalId);
        return request;
    }

    public static ApproveEInvoiceRequest create(List<String> approvalIds) {
        ApproveEInvoiceRequest request = new ApproveEInvoiceRequest();
        request.approvalIds = approvalIds;
        return request;
    }
}
