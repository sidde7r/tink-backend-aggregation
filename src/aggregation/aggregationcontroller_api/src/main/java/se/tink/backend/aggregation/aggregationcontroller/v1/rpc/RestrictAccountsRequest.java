package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.MoreObjects;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.jersey.utils.SafelyLoggable;

/**
 * Request used to pass information on which accounts needs to be removed because they have been
 * restricted due to regulatory restrictions (e.g. PSD2).
 */
@Data
@Accessors(chain = true)
public class RestrictAccountsRequest implements SafelyLoggable {
    private List<String> accountIds;
    private List<AccountTypes> accountTypes;
    private String credentialsId;
    private String userId;

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("accountIds", accountIds)
                .add("accountTypes", accountTypes)
                .add("credentialsId", credentialsId)
                .add("userId", userId)
                .toString();
    }
}
