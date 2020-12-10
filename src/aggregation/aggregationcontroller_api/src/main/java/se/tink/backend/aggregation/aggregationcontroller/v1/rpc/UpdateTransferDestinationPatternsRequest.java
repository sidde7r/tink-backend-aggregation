package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.rpc.Account;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UpdateTransferDestinationPatternsRequest {

    private String userId;
    private List<AccountWithDestinations> destinationsBySouce;
    private String credentialsId;

    public Map<Account, List<TransferDestinationPattern>> getDestinationsBySouce() {
        if (destinationsBySouce == null) {
            return null;
        }
        Map<Account, List<TransferDestinationPattern>> destinationsBySouce = Maps.newHashMap();
        for (AccountWithDestinations obj : this.destinationsBySouce) {
            destinationsBySouce.put(obj.account, obj.destinations);
        }
        return destinationsBySouce;
    }

    public void setDestinationsBySouce(
            Map<Account, List<TransferDestinationPattern>> destinationsBySouce) {
        if (destinationsBySouce == null) {
            this.destinationsBySouce = null;
            return;
        }

        this.destinationsBySouce = Lists.newArrayList();
        for (Account account : destinationsBySouce.keySet()) {
            AccountWithDestinations obj = new AccountWithDestinations();
            obj.account = account;
            obj.destinations = destinationsBySouce.get(account);
            this.destinationsBySouce.add(obj);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    private static class AccountWithDestinations {
        private Account account;
        private List<TransferDestinationPattern> destinations;
    }
}
