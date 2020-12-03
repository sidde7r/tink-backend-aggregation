package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.BalanceAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardEntity {

    private String resourceId;
    private String maskedPan;
    private String currency;
    private String product;
    private BalanceAmountEntity creditLimit;
}
