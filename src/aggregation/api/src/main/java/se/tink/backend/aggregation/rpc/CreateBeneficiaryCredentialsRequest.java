package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBeneficiaryCredentialsRequest extends CredentialsRequest {

    private final Beneficiary beneficiary;
    private final String ownerAccountNumber;
    private String refreshId;

    @JsonCreator
    public CreateBeneficiaryCredentialsRequest(
            @JsonProperty("user") User user,
            @JsonProperty("provider") Provider provider,
            @JsonProperty("credentials") Credentials credentials,
            @JsonProperty("beneficiary") Beneficiary beneficiary,
            @JsonProperty("ownerAccountNumber") String ownerAccountNumber) {
        super(user, provider, credentials);
        this.beneficiary = beneficiary;
        this.ownerAccountNumber = ownerAccountNumber;
    }

    public Beneficiary getBeneficiary() {
        return this.beneficiary;
    }

    public String getOwnerAccountNumber() {
        return this.ownerAccountNumber;
    }

    @Override
    public boolean isManual() {
        return true;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.CREATE_BENEFICIARY;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }
}
