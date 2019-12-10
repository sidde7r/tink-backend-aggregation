package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import java.util.LinkedList;
import java.util.List;

public class BancoBpiTransactionalAccountsInfo {

    private String nip;

    private List<TransactionalAccountBaseInfo> accountInfo = new LinkedList<>();

    public List<TransactionalAccountBaseInfo> getAccountInfo() {
        return accountInfo;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }
}
