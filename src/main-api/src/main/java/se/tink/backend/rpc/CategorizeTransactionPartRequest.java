package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.Transaction;

public class CategorizeTransactionPartRequest {
    @Tag(1)
    @ApiModelProperty(name = "categoryId", required = true)
    private String categoryId;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
