package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public class CategorizeTransactionsRequest {
    
    @ApiModelProperty(name = "categoryId", value="The internal identifier of the category that the list of transactions is categorized to.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    protected String categoryId;
    @ApiModelProperty(name = "transactionIds", value="A list of internal identifiers of the transactions categorized.", example = "[\"92e9e178cc22437281084c572ada8d7d\",\"a40db0b79bf94d2a9340cbc35d8b8020\"]", required = true)
    protected List<String> transactionIds;

    public CategorizeTransactionsRequest() {
    }

    public CategorizeTransactionsRequest(String categoryId, List<String> transactionIds) {
        this.categoryId = categoryId;
        this.transactionIds = transactionIds;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }
}
