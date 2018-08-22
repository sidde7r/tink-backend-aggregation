package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

// "Normal" accounts only
// filter out any loans when converting
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterAccountsResponse extends ArrayList<SdcAccount> {

    @JsonIgnore
    public Collection<TransactionalAccount> getTinkAccounts(SdcConfiguration agentConfiguration) {
        return stream()
                .filter(a -> a.isTransactionalAccount(agentConfiguration))
                .map(a -> a.toTinkAccount(agentConfiguration))
                .collect(Collectors.toList());
    }

    public SdcAccount findAccount(SdcCreditCardEntity creditCardEntity) {
        return stream()
                .filter(creditCardEntity::belongsTo)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No attached account found"));
    }
}
