package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.uk.barclays.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse extends Response {
    private String hasMoreAccountsMsg;
    private boolean businessSoldAvailable;
    private boolean Is_PinViewMVCAllowed;
    // mortgagePlaceholderAccount > unknown type
    private boolean commercialCardFlag;
    private List<AccountEntity> accounts;

    public String getHasMoreAccountsMsg() {
        return hasMoreAccountsMsg;
    }

    public boolean isBusinessSoldAvailable() {
        return businessSoldAvailable;
    }

    public boolean isIs_PinViewMVCAllowed() {
        return Is_PinViewMVCAllowed;
    }

    public boolean isCommercialCardFlag() {
        return commercialCardFlag;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
