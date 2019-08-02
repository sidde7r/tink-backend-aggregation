package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class DebtorEntity {
    private String name;
    private PostalAddressEntity postalAddress;
    private IdEntity privateId;

    @JsonIgnore
    public static DebtorEntity of(PaymentRequest paymentRequest) {
        return new DebtorEntity(paymentRequest.getPayment().getCreditor().getName());
    }

    @JsonCreator
    public DebtorEntity(String name, PostalAddressEntity postalAddress, IdEntity privateId) {
        this.name = name;
        this.postalAddress = postalAddress;
        this.privateId = privateId;
    }

    @JsonIgnore
    public DebtorEntity(String name) {
        this.name = name;
    }
}
