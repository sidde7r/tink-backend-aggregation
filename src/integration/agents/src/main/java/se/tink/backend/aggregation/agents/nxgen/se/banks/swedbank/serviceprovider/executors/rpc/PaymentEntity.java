package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PaymentEntity {
    private String type;
    private String status;
    private ReferenceEntity reference;
    private PayeeEntity payee;
    private String dateDependency;
    private String withdrawalDate;
    private String rejectionMessage;
}
