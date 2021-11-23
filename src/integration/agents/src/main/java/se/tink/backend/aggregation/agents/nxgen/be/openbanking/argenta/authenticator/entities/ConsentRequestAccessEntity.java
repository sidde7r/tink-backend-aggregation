package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class ConsentRequestAccessEntity {

    private List<IbanEntity> accounts;
    private List<IbanEntity> balances;
    private List<IbanEntity> transactions;
    private AdditionalInformationEntity additionalInformation;

    public ConsentRequestAccessEntity(List<IbanEntity> ibans) {
        this.accounts = ibans;
        this.balances = ibans;
        this.transactions = ibans;
        this.additionalInformation = new AdditionalInformationEntity(ibans);
    }
}
