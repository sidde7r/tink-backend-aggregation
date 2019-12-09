package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class RemoteAccount {
    private String bic;
    private String iban;
    private String name;

    public RemoteAccount(String bic, String iban, String name) {
        this.bic = bic;
        this.iban = iban;
        this.name = name;
    }
}
