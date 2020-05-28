package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.AddBeneficiary;

public class CreateBeneficiaryMultiStepRequest extends AddBeneficiaryRequest {
    private String step;
    private final List<Field> fields;
    private List<String> userInputs;
    private AddBeneficiary beneficiary;

    public CreateBeneficiaryMultiStepRequest(
            AddBeneficiary beneficiary,
            Storage storage,
            String step,
            List<Field> fields,
            List<String> userInputs) {
        super(beneficiary, storage);
        this.step = step;
        this.fields = fields;
        this.userInputs = userInputs;
        this.beneficiary = beneficiary;
    }

    public static CreateBeneficiaryMultiStepRequest of(
            AddBeneficiaryResponse addBeneficiaryResponse) {
        return new CreateBeneficiaryMultiStepRequest(
                addBeneficiaryResponse.getBeneficiary(),
                Storage.copyOf(addBeneficiaryResponse.getStorage()),
                SigningStepConstants.STEP_INIT,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public String getStep() {
        return step;
    }

    public List<String> getUserInputs() {
        return userInputs;
    }
}
