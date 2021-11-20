package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc;

import java.util.Collection;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelAccountsResponse {

    private Collection<NickelAccount> accounts;
}
