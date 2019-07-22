package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.BaseSignRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EInvoiceSignRequest implements BaseSignRequest {
    private List<String> approvalIds;

    public static EInvoiceSignRequest create(String approvalId) {
        EInvoiceSignRequest request = new EInvoiceSignRequest();
        request.approvalIds = new ArrayList<>();
        request.approvalIds.add(approvalId);
        return request;
    }

    public static EInvoiceSignRequest create(List<String> approvalIds) {
        EInvoiceSignRequest request = new EInvoiceSignRequest();
        request.approvalIds = approvalIds;
        return request;
    }
}
