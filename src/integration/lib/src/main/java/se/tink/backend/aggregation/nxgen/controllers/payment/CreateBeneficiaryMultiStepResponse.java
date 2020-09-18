package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

@Getter
public class CreateBeneficiaryMultiStepResponse extends CreateBeneficiaryResponse {
    private String step;
    private List<Field> fields;

    public CreateBeneficiaryMultiStepResponse(
            CreateBeneficiary beneficiary, Storage storage, String step, List<Field> fields) {
        super(beneficiary, storage);
        this.step = step;
        this.fields = fields;
    }

    public CreateBeneficiaryMultiStepResponse(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest,
            String step,
            List<Field> fields) {
        super(
                createBeneficiaryMultiStepRequest.getBeneficiary(),
                createBeneficiaryMultiStepRequest.getStorage());
        this.step = step;
        this.fields = fields;
    }

    public CreateBeneficiaryMultiStepResponse(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest, String step) {
        this(createBeneficiaryMultiStepRequest, step, Collections.emptyList());
    }

    public CreateBeneficiaryMultiStepResponse(
            CreateBeneficiaryResponse createBeneficiaryResponse,
            String step,
            ArrayList<Field> fields) {
        super(createBeneficiaryResponse.getBeneficiary(), createBeneficiaryResponse.getStorage());
        this.step = step;
        this.fields = fields;
    }
}
