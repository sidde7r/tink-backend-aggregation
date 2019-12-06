package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Unstructured remittance information uses EACT format.
 * Description matching RE agent in /TXT/ field, prefixed by "D|" or "H|"
 */
public class BankinterTransactionEntity extends EactParsingTransactionEntity {
    private static final Logger LOG = LoggerFactory.getLogger(BankinterTransactionEntity.class);

    @Override
    @JsonIgnore
    public String getDescription() {
        final String text = super.getDescription();
        if (text.startsWith("D|") || text.startsWith("H|")) {
            return text.substring(2);
        } else {
            LOG.warn(
                    "Could not parse transaction description: {}",
                    remittanceInformationUnstructured);
            return text;
        }
    }
}
