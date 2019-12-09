package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

public class CaixabankTransactionEntity extends TransactionEntity {

    @Override
    @JsonIgnore
    protected Date getDate() {
        // Caixabank RE agent uses value date for transaction date
        return valueDate;
    }
}
