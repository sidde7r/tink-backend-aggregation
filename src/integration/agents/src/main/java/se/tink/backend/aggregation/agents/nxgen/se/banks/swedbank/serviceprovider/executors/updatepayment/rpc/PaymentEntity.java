package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentEntity {
    private String type;
    private String status;
    private ReferenceEntity reference;
    private PayeeEntity payee;
    private String dateDependency;
    private String withdrawalDate;
    private String rejectionMessage;

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public ReferenceEntity getReference() {
        return reference;
    }

    public PayeeEntity getPayee() {
        return payee;
    }

    public String getDateDependency() {
        return dateDependency;
    }

    public String getWithdrawalDate() {
        return withdrawalDate;
    }

    public String getRejectionMessage() {
        return rejectionMessage;
    }
}
