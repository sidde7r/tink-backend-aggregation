package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessEntity {

    @JsonIgnore private final List<ConsentPayloadEntity> ibans;

    public ConsentAccessEntity(String iban) {
        this.ibans = Arrays.asList(new ConsentPayloadEntity(iban));
    }

    @JsonGetter
    List<ConsentPayloadEntity> getAccounts() {
        return ibans;
    }

    @JsonGetter
    List<ConsentPayloadEntity> getTransactions() {
        return ibans;
    }

    @JsonGetter
    List<ConsentPayloadEntity> getBalances() {
        return ibans;
    }
}
