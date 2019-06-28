package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities.EntryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class CreateDraftPaymentRequest {
    private List<EntryEntity> entries;

    @JsonProperty("number_of_required_accepts")
    private int numberOfRequiredAccepts = 1;

    public CreateDraftPaymentRequest(List<EntryEntity> entries) {
        this.entries = entries;
    }

    public static CreateDraftPaymentRequest of(PaymentRequest paymentRequest)
            throws PaymentException {

        EntryEntity entry = EntryEntity.of(paymentRequest);

        return new CreateDraftPaymentRequest(Arrays.asList(entry));
    }
}
