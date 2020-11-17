package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceV31Response extends BaseV31Response<List<AccountBalanceEntity>> {}
