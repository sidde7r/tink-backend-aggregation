package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import java.util.LinkedList;
import java.util.List;

public class BancoBpiAccountsContext {

    private List<TransactionalAccountBaseInfo> accountInfo = new LinkedList<>();

    public List<TransactionalAccountBaseInfo> getAccountInfo() {
        return accountInfo;
    }
}
