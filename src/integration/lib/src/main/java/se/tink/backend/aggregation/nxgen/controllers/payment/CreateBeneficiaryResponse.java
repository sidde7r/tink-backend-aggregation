package se.tink.backend.aggregation.nxgen.controllers.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

@Getter
@Setter
@AllArgsConstructor
public class CreateBeneficiaryResponse {
    private CreateBeneficiary beneficiary;
    private Storage storage;

    public CreateBeneficiaryResponse(CreateBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
        this.storage = new Storage();
    }

    public static CreateBeneficiaryResponse of(CreateBeneficiaryRequest createBeneficiaryRequest) {
        return new CreateBeneficiaryResponse(
                createBeneficiaryRequest.getBeneficiary(),
                Storage.copyOf(createBeneficiaryRequest.getStorage()));
    }
}
