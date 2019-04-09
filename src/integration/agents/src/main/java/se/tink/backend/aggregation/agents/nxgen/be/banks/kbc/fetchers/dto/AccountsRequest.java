package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountsRequest {
    private final TypeValuePair balanceIndicator;
    private final TypeValuePair includeReservationsIndicator;
    private final TypeValuePair includeAgreementMakeUp;
    private final TypeValuePair retrieveSavingsAccountsOnlyIndicator;
    private final TypeValuePair retrieveCurrentAccountsOnlyIndicator;
    private final TypeValuePair paymentDashboardIndicator;

    public AccountsRequest(
            TypeValuePair balanceIndicator,
            TypeValuePair includeReservationsIndicator,
            TypeValuePair includeAgreementMakeUp,
            TypeValuePair retrieveSavingsAccountsOnlyIndicator,
            TypeValuePair retrieveCurrentAccountsOnlyIndicator,
            TypeValuePair paymentDashboardIndicator) {
        this.balanceIndicator = balanceIndicator;
        this.includeReservationsIndicator = includeReservationsIndicator;
        this.includeAgreementMakeUp = includeAgreementMakeUp;
        this.retrieveSavingsAccountsOnlyIndicator = retrieveSavingsAccountsOnlyIndicator;
        this.retrieveCurrentAccountsOnlyIndicator = retrieveCurrentAccountsOnlyIndicator;
        this.paymentDashboardIndicator = paymentDashboardIndicator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeValuePair balanceIndicator;
        private TypeValuePair includeReservationsIndicator;
        private TypeValuePair includeAgreementMakeUp;
        private TypeValuePair retrieveSavingsAccountsOnlyIndicator;
        private TypeValuePair retrieveCurrentAccountsOnlyIndicator;
        private TypeValuePair paymentDashboardIndicator;

        public Builder setBalanceIndicator(boolean balanceIndicator) {
            this.balanceIndicator = TypeValuePair.createBoolean(balanceIndicator);
            return this;
        }

        public Builder setIncludeReservationsIndicator(boolean includeReservationsIndicator) {
            this.includeReservationsIndicator =
                    TypeValuePair.createBoolean(includeReservationsIndicator);
            return this;
        }

        public Builder setIncludeAgreementMakeUp(boolean includeAgreementMakeUp) {
            this.includeAgreementMakeUp = TypeValuePair.createBoolean(includeAgreementMakeUp);
            return this;
        }

        public Builder setRetrieveSavingsAccountsOnlyIndicator(
                boolean retrieveSavingsAccountsOnlyIndicator) {
            this.retrieveSavingsAccountsOnlyIndicator =
                    TypeValuePair.createBoolean(retrieveSavingsAccountsOnlyIndicator);
            return this;
        }

        public Builder setRetrieveCurrentAccountsOnlyIndicator(
                boolean retrieveCurrentAccountsOnlyIndicator) {
            this.retrieveCurrentAccountsOnlyIndicator =
                    TypeValuePair.createBoolean(retrieveCurrentAccountsOnlyIndicator);
            return this;
        }

        public Builder setPaymentDashboardIndicator(boolean paymentDashboardIndicator) {
            this.paymentDashboardIndicator = TypeValuePair.createBoolean(paymentDashboardIndicator);
            return this;
        }

        public AccountsRequest build() {
            return new AccountsRequest(
                    balanceIndicator,
                    includeReservationsIndicator,
                    includeAgreementMakeUp,
                    retrieveSavingsAccountsOnlyIndicator,
                    retrieveCurrentAccountsOnlyIndicator,
                    paymentDashboardIndicator);
        }
    }
}
