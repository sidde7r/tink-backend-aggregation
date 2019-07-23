package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.DataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferApprovalRequest {

    private DataEntity data;
    private String formId;
    private String type;

    private TransferApprovalRequest() {}

    @JsonIgnore
    public static TransferApprovalRequest create(String transferData, String formId) {
        TransferApprovalRequest request = new TransferApprovalRequest();
        request.data = new DataEntity().setTransferData(transferData);
        request.formId = formId;
        request.type = "FORM_DATA";
        return request;
    }
}
