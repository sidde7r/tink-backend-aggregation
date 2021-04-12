package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalancesItemEntity {

    private String balanceType;

    private String name;

    private String lastChangeDateTime;

    private BalanceAmountEntity balanceAmount;

    private String referenceDate;
}
