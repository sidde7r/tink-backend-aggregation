package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.RecurringPayment;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringPaymentRequest extends TransferRequest {

    public RecurringPaymentRequest() {}

    public RecurringPaymentRequest(
            User user,
            Provider provider,
            Credentials credentials,
            SignableOperation signableOperation,
            boolean update) {
        super(user, provider, credentials, signableOperation, update);
    }

    @JsonIgnore
    public RecurringPayment getRecurringPayment() {
        return this.getSignableOperation().getSignableObject(RecurringPayment.class);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.RECURRING_PAYMENT;
    }
}
