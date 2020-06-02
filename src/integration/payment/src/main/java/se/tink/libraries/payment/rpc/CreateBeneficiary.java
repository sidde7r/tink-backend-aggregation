package se.tink.libraries.payment.rpc;

import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

public class CreateBeneficiary {
    private Beneficiary beneficiary;
    private CreateBeneficiaryStatus status;

    private CreateBeneficiary(Builder builder) {
        this.beneficiary = builder.beneficiary;
        this.status = builder.status;
    }

    public CreateBeneficiaryStatus getStatus() {
        return status;
    }

    public void setStatus(CreateBeneficiaryStatus status) {
        this.status = status;
    }

    public static class Builder {
        private Beneficiary beneficiary;
        private CreateBeneficiaryStatus status;

        public Builder withBeneficiary(Beneficiary beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public Builder withStatus(CreateBeneficiaryStatus status) {
            this.status = status;
            return this;
        }

        public CreateBeneficiary build() {
            return new CreateBeneficiary(this);
        }
    }
}
