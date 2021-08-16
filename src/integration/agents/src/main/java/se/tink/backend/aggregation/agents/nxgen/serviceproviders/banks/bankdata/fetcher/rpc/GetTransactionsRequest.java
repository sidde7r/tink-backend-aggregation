package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetTransactionsRequest {
    // fixed, always false
    private boolean onlyUnCategorized = false;
    private boolean ascendingOrder = false;
    private boolean showPlanning = false;
    private boolean onlyFlagged = false;
    // not fixed
    private int page;
    private List<BankdataAccountIdEntity> accounts = new ArrayList<>();

    public GetTransactionsRequest setPage(int page) {
        this.page = page;
        return this;
    }

    @JsonIgnore
    public GetTransactionsRequest addAccount(String regNo, String accountNo) {
        BankdataAccountIdEntity accountIdEntity =
                new BankdataAccountIdEntity().setRegNo(regNo).setAccountNo(accountNo);

        accounts.add(accountIdEntity);
        return this;
    }
}
