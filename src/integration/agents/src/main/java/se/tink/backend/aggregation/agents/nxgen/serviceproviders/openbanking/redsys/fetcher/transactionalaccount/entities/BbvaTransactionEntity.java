package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* In order to make the description look like the re we need to split it up and only return the last part
 * The other part let us know their categorization
 */
public class BbvaTransactionEntity extends EactParsingTransactionEntity {
    private static final Logger LOG = LoggerFactory.getLogger(BbvaTransactionEntity.class);

    @Override
    @JsonIgnore
    public String getDescription() {
        final String transactionText = super.getDescription();
        final String[] splitTransactionText = transactionText.split("//");
        if (splitTransactionText.length != 3) {
            LOG.warn("Transaction description has been changed!");
        }

        return splitTransactionText[2];
    }
}
