package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LaBanquePortalBerlinGroupAccountBaseResponse extends AccountsBaseResponseBerlinGroup {

    public LaBanquePortalBerlinGroupAccountBaseResponse(List<AccountEntityBaseEntity> accounts) {}
}
