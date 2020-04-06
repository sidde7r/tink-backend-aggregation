package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype;

import java.util.HashMap;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail.DefaultAccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail.IngDibaAccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

public class FinTsAccountTypeMapper {

    private static final HashMap<Bank, AccountTypeMapper> DISPATCH_MAP = new HashMap<>();
    private static final AccountTypeMapper DEFAULT_MAPPER = new DefaultAccountTypeMapper();

    public Optional<AccountTypes> getAccountTypeFor(
            FinTsDialogContext dialogContext, HIUPD basicAccountInformation) {
        Bank bank = dialogContext.getConfiguration().getBank();
        AccountTypeMapper mapper = DISPATCH_MAP.getOrDefault(bank, DEFAULT_MAPPER);
        return mapper.getAccountTypeFor(basicAccountInformation);
    }

    static {
        DISPATCH_MAP.put(Bank.ING_DIBA, new IngDibaAccountTypeMapper());
    }
}
