package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler.SdcAccountIdentifierHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

// "Normal" accounts only
// filter out any loans when converting
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterAccountsResponse extends ArrayList<SdcAccount> {

    @JsonIgnore
    public Collection<TransactionalAccount> getTinkAccounts(
            final SdcAccountIdentifierHandler accountIdentifierHandler) {
        return stream()
                .filter(SdcAccount::isTransactionalAccount)
                .map(a -> a.toTinkAccount(accountIdentifierHandler))
                .collect(Collectors.toList());
    }

    public SdcAccount findAccount(SdcCreditCardEntity creditCardEntity) {
        return stream()
                .filter(creditCardEntity::belongsTo)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No attached account found"));
    }
}
