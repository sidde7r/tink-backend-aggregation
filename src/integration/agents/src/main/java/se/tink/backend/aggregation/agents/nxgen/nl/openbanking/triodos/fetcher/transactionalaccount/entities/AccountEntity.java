package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.transactionalaccount.entities;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

public class AccountEntity extends AccountEntityBaseEntity {

    @Override
    public List<Party> getParties() {
        return TriodosConstants.HOLDER_NAME_SPLITTER
                .splitAsStream(getName().trim())
                .map(name -> new Party(name, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }
}
