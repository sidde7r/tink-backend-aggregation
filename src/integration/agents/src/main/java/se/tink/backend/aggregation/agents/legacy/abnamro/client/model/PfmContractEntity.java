package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PfmContractEntity {
    private Integer buildingBlockId; // Remove in the future
    private AmountEntity balance;
    private String contractNumber;
    private String name;
    private String accountNumber;
    private String productGroup;

    public Integer getBuildingBlockId() {
        return buildingBlockId;
    }

    public void setBuildingBlockId(Integer buildingBlockId) {
        this.buildingBlockId = buildingBlockId;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public void setBalance(AmountEntity balance) {
        this.balance = balance;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }
}
