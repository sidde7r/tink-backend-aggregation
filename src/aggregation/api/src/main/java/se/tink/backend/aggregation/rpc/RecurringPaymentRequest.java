package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringPaymentRequest extends CredentialsRequest {

    private SignableOperation signableOperation;
    private boolean update;
    private boolean skipRefresh;

    private LocalDate firstPaymentDate;
    private Integer dayOfMonth;

    public RecurringPaymentRequest() {}

    public RecurringPaymentRequest(
            User user,
            Provider provider,
            Credentials credentials,
            SignableOperation signableOperation,
            boolean update) {
        super(user, provider, credentials);

        this.signableOperation = signableOperation;
        this.update = update;
    }

    public SignableOperation getSignableOperation() {
        return signableOperation;
    }

    @JsonIgnore
    public Transfer getTransfer() {
        return signableOperation.getSignableObject(Transfer.class);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.RECURRING_PAYMENT;
    }

    public void setSignableOperation(SignableOperation signableOperation) {
        this.signableOperation = signableOperation;
    }

    @Override
    public boolean isManual() {
        return true;
    }

    @Override
    public boolean isUpdate() {
        return update;
    }

    @Override
    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isSkipRefresh() {
        return skipRefresh;
    }

    public void setSkipRefresh(boolean skipRefresh) {
        this.skipRefresh = skipRefresh;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }
}
