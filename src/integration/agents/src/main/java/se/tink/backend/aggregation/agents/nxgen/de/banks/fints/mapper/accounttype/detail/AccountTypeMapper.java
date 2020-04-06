package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

public interface AccountTypeMapper {
    Optional<AccountTypes> getAccountTypeFor(HIUPD basicAccountInformation);
}
