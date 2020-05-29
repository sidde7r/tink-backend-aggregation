package se.tink.backend.aggregation.rpc;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.user.rpc.User;

public class CreateBeneficiaryCredentialsRequest extends CredentialsRequest {

    private final Beneficiary beneficiary;

    public CreateBeneficiaryCredentialsRequest(
            User user, Provider provider, Credentials credentials, Beneficiary beneficiary) {
        super(user, provider, credentials);
        this.beneficiary = beneficiary;
    }

    public Beneficiary getBeneficiary() {
        return this.beneficiary;
    }

    @Override
    public boolean isManual() {
        return true;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.CREATE_BENEFICIARY;
    }
}
