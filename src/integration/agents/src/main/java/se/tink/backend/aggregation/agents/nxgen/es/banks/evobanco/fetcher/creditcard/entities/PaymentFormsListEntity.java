package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentFormsListEntity {
    @JsonProperty("formaPago")
    private String paymentForm;

    @JsonProperty("descripFormasPago")
    private String descripPaymentForms;

    @JsonProperty("interesesFormaPago")
    private String interestFormPayment;
}
