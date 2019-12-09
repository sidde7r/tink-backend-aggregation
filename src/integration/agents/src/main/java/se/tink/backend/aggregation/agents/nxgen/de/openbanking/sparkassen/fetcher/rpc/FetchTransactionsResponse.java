package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml.BkToCstmrAcctRptEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml.EntryEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml.RptEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@XmlRootElement(name = "Document")
public class FetchTransactionsResponse {

    @XmlElement(name = "BkToCstmrAcctRpt")
    private BkToCstmrAcctRptEntity bkToCstmrAcctRpt;

    public AggregationTransaction toTinkTransaction() {

        RptEntity rpt = bkToCstmrAcctRpt.getRpt();

        EntryEntity entryEntity =
                rpt.getEntries().stream()
                        .filter(EntryEntity::isDebit)
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);

        return Transaction.builder()
                .setPending(!entryEntity.isBooked())
                .setAmount(
                        ExactCurrencyAmount.of(
                                entryEntity.getAmount().getValue(),
                                entryEntity.getAmount().getCurrency()))
                .setDescription(entryEntity.getDescription())
                .setDate(LocalDate.parse(entryEntity.getValueDate().getDate()))
                .build();
    }
}
