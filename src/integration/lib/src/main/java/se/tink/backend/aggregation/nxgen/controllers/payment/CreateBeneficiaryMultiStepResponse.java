package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.AddBeneficiary;

public class CreateBeneficiaryMultiStepResponse extends AddBeneficiaryResponse {
    private String step;
    private List<Field> fields;

    public CreateBeneficiaryMultiStepResponse(
            AddBeneficiary beneficiary, Storage storage, String step, List<Field> fields) {
        super(beneficiary, storage);
        this.step = step;
        this.fields = fields;
    }

    public CreateBeneficiaryMultiStepResponse(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest,
            String step,
            ArrayList<Field> fields) {
        super(
                createBeneficiaryMultiStepRequest.getBeneficiary(),
                createBeneficiaryMultiStepRequest.getStorage());
        this.step = step;
        this.fields = fields;
    }

    public CreateBeneficiaryMultiStepResponse(
            AddBeneficiaryResponse addBeneficiaryResponse, String step, ArrayList<Field> fields) {
        super(addBeneficiaryResponse.getBeneficiary(), addBeneficiaryResponse.getStorage());
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
