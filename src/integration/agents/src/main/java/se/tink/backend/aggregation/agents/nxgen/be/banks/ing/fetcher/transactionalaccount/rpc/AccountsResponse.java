package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc;

import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.CategoryListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.MemoDatesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "mobileResponse")
public class AccountsResponse {
    private CategoryListEntity categories;
    private AccountListEntity accounts;
    private MemoDatesEntity memoDates;
    private String minimumAmount;

    public CategoryListEntity getCategories() {
        return categories;
    }

    public void setCategories(CategoryListEntity categories) {
        this.categories = categories;
    }

    public AccountListEntity getAccounts() {
        return accounts;
    }

    public void setAccounts(AccountListEntity accounts) {
        this.accounts = accounts;
    }

    public MemoDatesEntity getMemoDates() {
        return memoDates;
    }

    public void setMemoDates(MemoDatesEntity memoDates) {
        this.memoDates = memoDates;
    }

    public String getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        this.minimumAmount = minimumAmount;
    }
}
