package se.tink.backend.aggregationcontroller.v1.rpc.system.update;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Account;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.TransferDestinationPattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UpdateTransferDestinationPatternsRequest {
    private String userId;
    @JsonProperty("destinationsBySouce")
    private List<AccountWithDestinations> destinationsBySource;

    public Map<Account, List<TransferDestinationPattern>> getDestinationsBySource() {
        if (destinationsBySource == null) {
            return null;
        }
        Map<Account, List<TransferDestinationPattern>> destinationsBySouce = Maps.newHashMap();
        for(AccountWithDestinations obj : this.destinationsBySource) {
            destinationsBySouce.put(obj.account, obj.destinations);
        }
        return destinationsBySouce;
    }

    public void setDestinationsBySource(Map<Account, List<TransferDestinationPattern>> destinationsBySource) {
        if (destinationsBySource == null) {
            this.destinationsBySource = null;
            return;
        }

        this.destinationsBySource = Lists.newArrayList();
        for(Account account : destinationsBySource.keySet()) {
            AccountWithDestinations obj = new AccountWithDestinations();
            obj.account = account;
            obj.destinations = destinationsBySource.get(account);
            this.destinationsBySource.add(obj);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private static class AccountWithDestinations {
        public Account account;
        public List<TransferDestinationPattern> destinations;
    }
}
