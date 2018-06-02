package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SwedbankAccountEntity implements GeneralAccountEntity {

    private String accountNumber;
    private String id;
    private String name;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String generalGetName() {
        return this.name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("accountNumber", accountNumber).toString();
    }

    public enum SwedbankPaymentType {
        BGACCOUNT,
        PGACCOUNT
    }
}
