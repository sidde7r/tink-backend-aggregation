package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.DebtorCreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class PaymentSetupV11Request {
    @JsonProperty("Data")
    private DataEntity data;
    @JsonProperty("Risk")
    private RiskEntity risk;

    @JsonIgnore
    private PaymentSetupV11Request(DataEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    @JsonIgnore
    public static PaymentSetupV11Request createPersonToPerson(
            String internalTransferId,
            String externalTransferId,
            DebtorCreditorAccountEntity source,
            DebtorCreditorAccountEntity destination,
            Amount amount,
            String bankTransferMessage) {

        DataEntity dataEntity = DataEntity.createPersonToPerson(
                internalTransferId,
                externalTransferId,
                source,
                destination,
                amount,
                bankTransferMessage
        );

        return new PaymentSetupV11Request(dataEntity, RiskEntity.createPersonToPerson());
    }
}

