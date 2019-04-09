package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public class ContractEntity {

    private static final String STATUS_EXPIRED = "EXPIRED";

    private String accountNumber;
    private List<ActionEntity> actions;
    private AmountEntity balance;

    @JsonProperty("isBlocked")
    private boolean blocked;

    private String chid;
    private String concerning;
    private String contractNumber;
    private CustomerEntity customer;
    private String id;
    private ParentContractEntity parentContract;
    private ProductEntity product;
    private String resourceType;
    private Long sequenceNumber;
    private String status;

    public String getAccountNumber() {
        return accountNumber;
    }

    public List<ActionEntity> getActions() {
        return actions;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getChid() {
        return chid;
    }

    public String getConcerning() {
        return concerning;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public String getId() {
        return id;
    }

    public ParentContractEntity getParentContract() {
        return parentContract;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setActions(List<ActionEntity> actions) {
        this.actions = actions;
    }

    public void setBalance(AmountEntity balance) {
        this.balance = balance;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setChid(String chid) {
        this.chid = chid;
    }

    public void setConcerning(String concerning) {
        this.concerning = concerning;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParentContract(ParentContractEntity parentContract) {
        this.parentContract = parentContract;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContractEntity that = (ContractEntity) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isExpired() {
        return Objects.equals(status, STATUS_EXPIRED);
    }
}
