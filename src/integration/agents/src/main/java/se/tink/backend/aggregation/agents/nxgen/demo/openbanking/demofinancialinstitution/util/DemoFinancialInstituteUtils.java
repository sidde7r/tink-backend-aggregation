package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.util;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class DemoFinancialInstituteUtils {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
      TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "transactional").build();
}
