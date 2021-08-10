package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc;

import java.math.BigInteger;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AccountHolderResponse {
    private String name;
    private String personType;
    private BigInteger personCode;
    private Integer interventionOrder;
    private String interventionType;
    private Integer responsibilityPercentage;
    private boolean removable;
}
