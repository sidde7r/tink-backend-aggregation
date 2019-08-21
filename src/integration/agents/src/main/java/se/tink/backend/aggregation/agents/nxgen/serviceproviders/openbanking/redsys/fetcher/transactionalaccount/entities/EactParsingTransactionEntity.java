package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.utils.eact.UnstructuredRemittanceInformation;

public class EactParsingTransactionEntity extends TransactionEntity {
    private static final Logger LOG = LoggerFactory.getLogger(BankinterTransactionEntity.class);

    @Override
    @JsonIgnore
    public String getDescription() {
        if (UnstructuredRemittanceInformation.matches(remittanceInformationUnstructured)) {
            // try to parse EACT-formatted unstructured remittance information
            try {
                return UnstructuredRemittanceInformation.parse(remittanceInformationUnstructured)
                        .getFreeText()
                        .orElse(remittanceInformationUnstructured);
            } catch (ParseException e) {
                LOG.warn(
                        "Could not parse EACT transaction description: {}",
                        remittanceInformationUnstructured);
                return remittanceInformationUnstructured;
            }
        } else {
            return remittanceInformationUnstructured;
        }
    }
}
