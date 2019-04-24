package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.payment.rpc.Payment;

public class CreateBeneficiaryMultiStepResponse {
    private String step;
    private List<Field> fields;

    public CreateBeneficiaryMultiStepResponse(Payment payment, String step, List<Field> fields) {
        this.step = step;
        this.fields = fields;
    }

    public String getStep() {
        return step;
    }

    public List<Field> getFields() {
        return fields;
    }
}
