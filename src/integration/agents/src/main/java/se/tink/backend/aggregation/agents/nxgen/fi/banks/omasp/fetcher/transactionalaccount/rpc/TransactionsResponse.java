package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;

@JsonObject
public class TransactionsResponse extends OmaspBaseResponse {
    private static final AggregationLogger log = new AggregationLogger(TransactionsResponse.class);

    private List<TransactionsEntity> transactions;
    private AmountEntity balance;
    private AmountEntity availableBalance;
    private String name;
    private String productName;
    private String ownerName;

    // for logging purposes
    private Object continuationSearchKey;

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public String getName() {
        return name;
    }

    public String getProductName() {
        return productName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Object getContinuationSearchKey() {
        return continuationSearchKey;
    }

    @JsonIgnore
    public AccountTypes getTinkAccountType(Credentials credentials) {
        AccountTypes type = OmaspConstants.ACCOUNT_TYPES.getOrDefault(productName.toLowerCase(), null);
        if (Objects.isNull(type)) {
            log.warn(String.format("%s: Unknown account type: %s",
                    OmaspConstants.LogTags.LOG_TAG_ACCOUNTS, productName));
            return AccountTypes.CHECKING;
        }
        return type;
    }
}
