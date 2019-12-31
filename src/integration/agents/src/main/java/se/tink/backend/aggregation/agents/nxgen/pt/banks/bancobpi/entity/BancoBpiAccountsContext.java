package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BancoBpiAccountsContext {

    private String nip;

    private List<TransactionalAccountBaseInfo> accountInfo = new LinkedList<>();

    public List<TransactionalAccountBaseInfo> getAccountInfo() {
        return accountInfo;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getNip() {
        return nip;
    }

    public String getNuc() {
        return accountInfo.get(0).getInternalAccountId();
    }

    public Optional<TransactionalAccountBaseInfo> findAccountInfoByNumber(String accountNumber) {
        return accountInfo.stream().filter(o -> o.getAccountName().equals(accountNumber)).findAny();
    }
}
