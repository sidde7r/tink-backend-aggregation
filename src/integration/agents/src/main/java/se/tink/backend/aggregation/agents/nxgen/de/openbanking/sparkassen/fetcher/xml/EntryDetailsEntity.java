package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class EntryDetailsEntity {

    @XmlElement(name = "TxDtls")
    private TransactionDetailsEntity transactionDetails;

    public TransactionDetailsEntity getTransactionDetails() {
        return transactionDetails;
    }
}
