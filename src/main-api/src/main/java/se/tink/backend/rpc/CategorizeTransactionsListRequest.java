package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public class CategorizeTransactionsListRequest {
    @Tag(1)
    @ApiModelProperty(name = "categorizationList", value="A list of new categories and the transactions' IDs", required = true)
    private List<CategorizeTransactionsRequest> categorizationList;

    public List<CategorizeTransactionsRequest> getCategorizationList() {
        return categorizationList;
    }

    public void setCategorizationList(List<CategorizeTransactionsRequest> categorizationList) {
        this.categorizationList = categorizationList;
    }
}
