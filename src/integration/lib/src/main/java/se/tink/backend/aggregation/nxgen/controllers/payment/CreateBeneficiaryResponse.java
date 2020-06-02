package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

public class CreateBeneficiaryResponse {
    private CreateBeneficiary beneficiary;
    private Storage storage;

    public CreateBeneficiaryResponse(CreateBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
        this.storage = new Storage();
    }

    public CreateBeneficiaryResponse(CreateBeneficiary beneficiary, Storage storage) {
        this.beneficiary = beneficiary;
        this.storage = Storage.copyOf(storage);
    }

    public static CreateBeneficiaryResponse of(CreateBeneficiaryRequest createBeneficiaryRequest) {
        return new CreateBeneficiaryResponse(
                createBeneficiaryRequest.getBeneficiary(),
                Storage.copyOf(createBeneficiaryRequest.getStorage()));
    }

    public CreateBeneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(CreateBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
