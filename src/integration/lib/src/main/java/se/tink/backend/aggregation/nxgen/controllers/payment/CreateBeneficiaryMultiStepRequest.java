package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

@Getter
public class CreateBeneficiaryMultiStepRequest extends CreateBeneficiaryRequest {
    private String step;
    private final List<Field> fields;
    private List<String> userInputs;
    private CreateBeneficiary beneficiary;

    public CreateBeneficiaryMultiStepRequest(
            CreateBeneficiary beneficiary,
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
            CreateBeneficiaryResponse createBeneficiaryResponse) {
        return new CreateBeneficiaryMultiStepRequest(
                createBeneficiaryResponse.getBeneficiary(),
                Storage.copyOf(createBeneficiaryResponse.getStorage()),
                SigningStepConstants.STEP_INIT,
                Collections.emptyList(),
                Collections.emptyList());
    }
}
