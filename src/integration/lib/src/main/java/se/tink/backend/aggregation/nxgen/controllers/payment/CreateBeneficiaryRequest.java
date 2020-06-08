package se.tink.backend.aggregation.nxgen.controllers.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

@Getter
@Setter
@AllArgsConstructor
public class CreateBeneficiaryRequest {
    private CreateBeneficiary beneficiary;
    private Storage storage;

    public CreateBeneficiaryRequest(CreateBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
        this.storage = new Storage();
    }

    public static CreateBeneficiaryRequest of(CreateBeneficiaryResponse createBeneficiaryResponse) {
        return new CreateBeneficiaryRequest(
                createBeneficiaryResponse.getBeneficiary(),
                Storage.copyOf(createBeneficiaryResponse.getStorage()));
    }
}
