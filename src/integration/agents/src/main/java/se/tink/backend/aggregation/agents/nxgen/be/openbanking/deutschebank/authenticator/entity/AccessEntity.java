package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    @JsonIgnore private final List<IbanEntity> ibans;

    public AccessEntity(String iban, String currency) {
        ibans = Collections.singletonList(new IbanEntity(iban, currency));
    }

    @JsonGetter
    List<IbanEntity> getAccounts() {
        return ibans;
    }

    @JsonGetter
    List<IbanEntity> getTransactions() {
        return ibans;
    }

    @JsonGetter
    List<IbanEntity> getBalances() {
        return ibans;
    }
}
