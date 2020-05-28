package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.AddBeneficiary;

public class AddBeneficiaryRequest {
    private AddBeneficiary beneficiary;
    private Storage storage;

    public AddBeneficiaryRequest(AddBeneficiary beneficiary) {
        this.beneficiary = beneficiary;
        this.storage = new Storage();
    }

    public AddBeneficiaryRequest(AddBeneficiary beneficiary, Storage storage) {
        this.beneficiary = beneficiary;
        this.storage = Storage.copyOf(storage);
    }

    public static AddBeneficiaryRequest of(AddBeneficiaryResponse addBeneficiaryResponse) {
        return new AddBeneficiaryRequest(
                addBeneficiaryResponse.getBeneficiary(),
                Storage.copyOf(addBeneficiaryResponse.getStorage()));
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
