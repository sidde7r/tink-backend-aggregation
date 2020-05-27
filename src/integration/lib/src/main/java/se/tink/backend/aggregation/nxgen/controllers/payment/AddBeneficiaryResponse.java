package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.AddBeneficiary;

public class AddBeneficiaryResponse {
    private AddBeneficiary beneficiary;
    private Storage storage;

    public AddBeneficiaryResponse(AddBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
        this.storage = new Storage();
    }

    public AddBeneficiaryResponse(AddBeneficiary beneficiary, Storage storage) {
        this.beneficiary = beneficiary;
        this.storage = Storage.copyOf(storage);
    }

    public static AddBeneficiaryResponse of(AddBeneficiaryRequest addBeneficiaryRequest) {
        return new AddBeneficiaryResponse(
                addBeneficiaryRequest.getBeneficiary(),
                Storage.copyOf(addBeneficiaryRequest.getStorage()));
    }

    public AddBeneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(AddBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
