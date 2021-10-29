package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaAccountLinksEntity {

    private ArkeaBalanceLinksEntity balancesLink;
    private ArkeaTransactionLinksEntity transactionsLink;
}
