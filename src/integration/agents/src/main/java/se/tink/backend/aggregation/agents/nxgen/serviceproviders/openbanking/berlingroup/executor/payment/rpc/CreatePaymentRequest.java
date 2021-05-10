package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites.AmountEntity;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private AmountEntity instructedAmount;
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String endToEndIdentification;

    public CreatePaymentRequest(Builder builder) {
        this.instructedAmount = builder.instructedAmount;
        this.debtorAccount = builder.debtorAccount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
        this.endToEndIdentification = RandomUtils.generateRandomNumericString(10);
    }

    public static class Builder {
        private AmountEntity instructedAmount;
        private AccountEntity debtorAccount;
        private AccountEntity creditorAccount;
        private String creditorName;

        public Builder withAmount(AmountEntity amount) {
            this.instructedAmount = amount;
            return this;
        }

        public Builder withDebtor(AccountEntity debtor) {
            this.debtorAccount = debtor;
            return this;
        }

        public Builder withCreditor(AccountEntity creditor) {
            this.creditorAccount = creditor;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
