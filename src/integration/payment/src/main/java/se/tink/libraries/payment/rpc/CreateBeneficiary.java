package se.tink.libraries.payment.rpc;

import lombok.Builder;
import lombok.Data;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

@Builder
@Data
public class CreateBeneficiary {
    private final String ownerAccountNumber;
    private final Beneficiary beneficiary;
    private CreateBeneficiaryStatus status;

    public void setStatus(CreateBeneficiaryStatus status) {
        this.status = status;
    }
}
