package se.tink.backend.rpc;

import io.protostuff.Tag;
import java.util.List;
import se.tink.backend.core.signableoperation.SignableOperation;

public class SignableOperationsResponse {
    @Tag(1)
    private List<SignableOperation> operations;

    public List<SignableOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<SignableOperation> operations) {
        this.operations = operations;
    }
}
