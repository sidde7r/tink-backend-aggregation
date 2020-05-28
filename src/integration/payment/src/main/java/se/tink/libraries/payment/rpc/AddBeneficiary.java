package se.tink.libraries.payment.rpc;

import se.tink.libraries.payment.enums.AddBeneficiaryStatus;

public class AddBeneficiary {
    private Beneficiary beneficiary;
    private AddBeneficiaryStatus status;

    private AddBeneficiary(Builder builder) {
        // this.beneficiary = builder.beneficiary;
        this.status = builder.status;
    }

    public AddBeneficiaryStatus getStatus() {
        return status;
    }

    public void setStatus(AddBeneficiaryStatus status) {
        this.status = status;
    }

    public static class Builder {
        private Beneficiary beneficiary;
        private AddBeneficiaryStatus status;

        public Builder withBeneficiary(Beneficiary beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public Builder withStatus(AddBeneficiaryStatus status) {
            this.status = status;
            return this;
        }

        public AddBeneficiary build() {
            return new AddBeneficiary(this);
        }
    }
}
