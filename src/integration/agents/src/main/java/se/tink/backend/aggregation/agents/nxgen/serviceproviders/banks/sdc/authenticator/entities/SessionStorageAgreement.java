package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SessionStorageAgreement {
    private String userNumber;
    private String agreementId;
    private String name;
    private List<String> accountBankIds;

    public SessionStorageAgreement() {}

    public SessionStorageAgreement(SdcAgreementEntityKey entityKey, String name) {
        this.userNumber = entityKey.getUserNumber();
        this.agreementId = entityKey.getAgreementNumber();
        this.name = name;
        this.accountBankIds = new ArrayList<>();
    }

    // check if we have a bankId for this agreement
    public boolean hasAccountBankId(String accountBankId) {
        return accountBankIds != null && accountBankIds.contains(accountBankId);
    }

    public String getUserNumber() {
        return userNumber;
    }

    public SessionStorageAgreement setUserNumber(String userNumber) {
        this.userNumber = userNumber;
        return this;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public SessionStorageAgreement setAgreementNumber(String agreementId) {
        this.agreementId = agreementId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void addAccountBankId(String accountBankId) {
        accountBankIds.add(accountBankId);
    }

    public List<String> getAccountBankIds() {
        return accountBankIds;
    }

    public SessionStorageAgreement setAccountBankIds(List<String> accountBankIds) {
        this.accountBankIds = accountBankIds;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userNumber", userNumber)
                .append("name", name)
                .append("agreementId", agreementId)
                .append("accountBankIds", accountBankIds)
                .toString();
    }
}
