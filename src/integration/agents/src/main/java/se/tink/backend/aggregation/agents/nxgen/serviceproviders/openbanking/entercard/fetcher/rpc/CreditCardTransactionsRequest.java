package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
public class CreditCardTransactionsRequest {

    private String accountNumber;
    private long startAtRowNum;
    private long stopAfterRowNum;
    private boolean includeCardMovements;
}
