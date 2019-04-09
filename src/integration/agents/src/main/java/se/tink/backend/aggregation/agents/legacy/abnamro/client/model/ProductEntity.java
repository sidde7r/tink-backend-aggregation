package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProductEntity {
    private String accountType;
    private long buildingBlockId;

    @JsonProperty("creditAccount")
    private boolean creditAcccount;

    private long id;
    private String name;
    private String productGroup;
    private String resourceType;
    private List<String> transferOptions;

    public String getAccountType() {
        return accountType;
    }

    public long getBuildingBlockId() {
        return buildingBlockId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public String getResourceType() {
        return resourceType;
    }

    public List<String> getTransferOptions() {
        return transferOptions;
    }

    public boolean isCreditAcccount() {
        return creditAcccount;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setBuildingBlockId(long buildingBlockId) {
        this.buildingBlockId = buildingBlockId;
    }

    public void setCreditAcccount(boolean creditAcccount) {
        this.creditAcccount = creditAcccount;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setTransferOptions(List<String> transferOptions) {
        this.transferOptions = transferOptions;
    }
}
