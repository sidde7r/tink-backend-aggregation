package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BankiaTransactionEntity extends EactParsingTransactionEntity {
    @Override
    @JsonIgnore
    // TXT field has one or two components separated by pipe, the first one is the description
    public String getDescription() {
        final String text = super.getDescription();
        final String components[] = text.split("\\|");
        return components[0];
    }
}
