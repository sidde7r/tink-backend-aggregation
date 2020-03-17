package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.party;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.BaseV31Response;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartyV31Response extends BaseV31Response<IdentityDataV31Entity> {}
