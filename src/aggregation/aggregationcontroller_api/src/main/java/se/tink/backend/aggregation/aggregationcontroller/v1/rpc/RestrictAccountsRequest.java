package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.agents.rpc.AccountTypes;

/**
 * Request used to pass information on which accounts needs to be removed because they have been
 * restricted due to regulatory restrictions (e.g. PSD2).
 */
@Data
@Accessors(chain = true)
public class RestrictAccountsRequest {
    private List<String> accountIds;
    private List<AccountTypes> accountTypes;
    private String credentialsId;
    private String userId;
}
