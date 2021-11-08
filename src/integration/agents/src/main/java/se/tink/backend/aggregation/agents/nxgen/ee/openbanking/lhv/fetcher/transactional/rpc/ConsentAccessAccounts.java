package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ConsentAccessAccounts {
    private String iban;
}
