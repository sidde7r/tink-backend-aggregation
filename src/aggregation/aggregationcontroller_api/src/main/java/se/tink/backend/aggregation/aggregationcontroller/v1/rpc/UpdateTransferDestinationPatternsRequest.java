package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.rpc.Account;
import se.tink.libraries.jersey.utils.SafelyLoggable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UpdateTransferDestinationPatternsRequest implements SafelyLoggable {

    private String userId;
    private List<AccountWithDestinations> destinationsBySouce;
    private String credentialsId;

    public Map<Account, List<TransferDestinationPattern>> getDestinationsBySouce() {
        if (destinationsBySouce == null) {
            return null;
        }

        return this.destinationsBySouce.stream()
                .collect(
                        Collectors.toMap(
                                AccountWithDestinations::getAccount,
                                AccountWithDestinations::getDestinations));
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

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("destinationsBySouce", destinationsBySouce)
                .add("credentialsId", credentialsId)
                .toString();
    }

    private static class AccountWithDestinations {
        private Account account;
        private List<TransferDestinationPattern> destinations;

        public Account getAccount() {
            return account;
        }

        public List<TransferDestinationPattern> getDestinations() {
            return destinations;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("account", account)
                    .add("destinations", destinations)
                    .toString();
        }
    }
}
