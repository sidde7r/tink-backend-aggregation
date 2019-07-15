package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchBalancesResponse {
    private List<AccountEntity> account;
    private String psuMessage;

    public List<AccountEntity> getAccount() {
        return account;
    }

    public void setAccount(List<AccountEntity> account) {
        this.account = account;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }
}
