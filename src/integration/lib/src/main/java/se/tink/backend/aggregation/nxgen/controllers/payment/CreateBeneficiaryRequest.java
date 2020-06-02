package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

public class CreateBeneficiaryRequest {
    private CreateBeneficiary beneficiary;
    private Storage storage;

    public CreateBeneficiaryRequest(CreateBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
        this.storage = new Storage();
    }

    public CreateBeneficiaryRequest(CreateBeneficiary beneficiary, Storage storage) {
        this.beneficiary = beneficiary;
        this.storage = Storage.copyOf(storage);
    }

    public static CreateBeneficiaryRequest of(CreateBeneficiaryResponse createBeneficiaryResponse) {
        return new CreateBeneficiaryRequest(
                createBeneficiaryResponse.getBeneficiary(),
                Storage.copyOf(createBeneficiaryResponse.getStorage()));
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
