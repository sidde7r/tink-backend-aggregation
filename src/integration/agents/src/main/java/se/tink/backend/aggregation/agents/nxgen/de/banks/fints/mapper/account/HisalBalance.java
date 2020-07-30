package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;

public interface HisalBalance {
    BalanceModule calculate(HISAL hisal);
}
