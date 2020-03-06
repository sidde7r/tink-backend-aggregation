package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HmacAccountIds {

    Map<String, HmacToken> accountIdToHmacToken;
}
