package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.libraries.account.AccountIdentifier;

public class PaymentsTestExecutionReport {
    private final List<Payment> paymentsAskedToRegister = new ArrayList<>();
    private final List<Payment> paymentsAskedToSign = new ArrayList<>();
    private final Set<Payment> paymentsAskedToGetSignStatus = new HashSet<>();
    private final List<AccountIdentifier> accountsAskedToFetchBeneficiariesFor = new ArrayList<>();
    private final List<Pair<AccountIdentifier, Beneficiary>> beneficiariesAskedToRegister =
            new ArrayList<>();
    private final List<Pair<AccountIdentifier, Beneficiary>> beneficiariesAskedToSign =
            new ArrayList<>();

    public List<Payment> getPaymentsAskedToRegister() {
        return this.paymentsAskedToRegister;
    }

    public List<Payment> getPaymentsAskedToSign() {
        return this.paymentsAskedToSign;
    }

    public Set<Payment> getPaymentsAskedToGetSignStatus() {
        return this.paymentsAskedToGetSignStatus;
    }

    public List<AccountIdentifier> getAccountsAskedToFetchBeneficiariesFor() {
        return accountsAskedToFetchBeneficiariesFor;
    }

    public List<Pair<AccountIdentifier, Beneficiary>> getBeneficiariesAskedToRegister() {
        return beneficiariesAskedToRegister;
    }

    public List<Pair<AccountIdentifier, Beneficiary>> getBeneficiariesAskedToSign() {
        return beneficiariesAskedToSign;
    }

    public void addPaymentsToRegister(List<Payment> payments) {
        this.paymentsAskedToRegister.addAll(payments);
    }

    public void addPaymentsToSign(List<Payment> payments) {
        this.paymentsAskedToSign.addAll(payments);
    }

    public void addPaymentsToGetSignStatus(List<Payment> payments) {
        this.paymentsAskedToGetSignStatus.addAll(payments);
    }

    public void addAccountToFetchBeneficiariesFor(AccountIdentifier debtorAccountIdentifier) {
        this.accountsAskedToFetchBeneficiariesFor.add(debtorAccountIdentifier);
    }

    public void addBeneficiaryToRegister(
            AccountIdentifier debtorAccountIdentifier, Beneficiary beneficiary) {
        this.beneficiariesAskedToRegister.add(Pair.of(debtorAccountIdentifier, beneficiary));
    }

    public void addBeneficiaryToSign(
            AccountIdentifier debtorAccountIdentifier, Beneficiary beneficiary) {
        this.beneficiariesAskedToSign.add(Pair.of(debtorAccountIdentifier, beneficiary));
    }
}
