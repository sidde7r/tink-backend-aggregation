package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.DebtorCreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class PaymentSubmissionV11Request {
    @JsonProperty("Data")
    private DataEntity data;
    @JsonProperty("Risk")
    private RiskEntity risk;

    @JsonIgnore
    private PaymentSubmissionV11Request(DataEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    @JsonIgnore
    public static PaymentSubmissionV11Request createPersonToPerson(
            String paymentId,
            String internalTransferId,
            String externalTransferId,
            DebtorCreditorAccountEntity source,
            DebtorCreditorAccountEntity destination,
            Amount amount,
            String transferReference) {

        DataEntity dataEntity = DataEntity.createPersonToPerson(
                paymentId,
                internalTransferId,
                externalTransferId,
                source,
                destination,
                amount,
                transferReference
        );

        return new PaymentSubmissionV11Request(dataEntity, RiskEntity.createPersonToPerson());
    }
}
