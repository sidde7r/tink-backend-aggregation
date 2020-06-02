package se.tink.libraries.payment.rpc;

import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

public class CreateBeneficiary {

    private final String ownerAccountNumber;
    private final Beneficiary beneficiary;
    private CreateBeneficiaryStatus status;

    private CreateBeneficiary(Builder builder) {
        this.beneficiary = builder.beneficiary;
        this.status = builder.status;
        this.ownerAccountNumber = builder.ownerAccountNumber;
    }

    public CreateBeneficiaryStatus getStatus() {
        return status;
    }

    public void setStatus(CreateBeneficiaryStatus status) {
        this.status = status;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public String getOwnerAccountNumber() {
        return ownerAccountNumber;
    }

    public static class Builder {
        private Beneficiary beneficiary;
        private CreateBeneficiaryStatus status;
        private String ownerAccountNumber;

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

        public Builder withOwnerAccountNumber(String ownerAccountNumber) {
            this.ownerAccountNumber = ownerAccountNumber;
            return this;
        }
    }
}
