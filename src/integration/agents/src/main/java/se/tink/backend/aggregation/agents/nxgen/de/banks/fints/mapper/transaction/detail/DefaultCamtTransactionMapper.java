package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.CreditDebitCode;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.Document;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.EntryStatus2Code;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.EntryTransaction2;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.ObjectFactory;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.PartyIdentification32;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.ReportEntry2;
import se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02.TransactionParty2;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultCamtTransactionMapper implements TransactionMapper {

    private static JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not initialize context");
        }
    }

    public List<AggregationTransaction> parse(String xmlCamtFormat) {
        Document document = toJavaObject(xmlCamtFormat);
        return toTinkTransactions(document);
    }

    private Document toJavaObject(String xmlCamtFormat) {
        try {
            Unmarshaller m = context.createUnmarshaller();
            JAXBElement<Document> unmarshal =
                    (JAXBElement<Document>) m.unmarshal(new StringReader(xmlCamtFormat));
            return unmarshal.getValue();
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not parse", e);
        }
    }

    private List<AggregationTransaction> toTinkTransactions(Document document) {
        return document.getBkToCstmrAcctRpt().getRpt().stream()
                .flatMap(x -> x.getNtry().stream())
                .map(this::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private AggregationTransaction toTinkTransaction(ReportEntry2 reportEntry) {
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        reportEntry.getAmt().getValue(), reportEntry.getAmt().getCcy());

        return Transaction.builder()
                .setPending(!isBooked(reportEntry))
                .setAmount(isDebit(reportEntry) ? amount.negate() : amount)
                .setDescription(getDescription(reportEntry))
                .setDate(LocalDate.parse(reportEntry.getValDt().getDt().toString()))
                .build();
    }

    private boolean isBooked(ReportEntry2 reportEntry) {
        return EntryStatus2Code.BOOK.equals(reportEntry.getSts());
    }

    private boolean isDebit(ReportEntry2 reportEntry) {
        return CreditDebitCode.DBIT.equals(reportEntry.getCdtDbtInd());
    }

    private String getDescription(ReportEntry2 reportEntry) {
        String beneficiary = getBeneficiary(reportEntry);

        String purpose =
                reportEntry.getNtryDtls().stream()
                        .flatMap(x -> x.getTxDtls().stream())
                        .flatMap(x -> x.getRmtInf().getUstrd().stream())
                        .findFirst()
                        .orElse(null);

        if (beneficiary == null
                || beneficiary.isEmpty()
                || beneficiary.toLowerCase().contains("paypal")) {
            // PayPal gets special treatment for now here in agent code, which isn't ideal.
            // ITE-1413 explains it a bit
            return purpose;
        } else {
            return beneficiary;
        }
    }

    private String getBeneficiary(ReportEntry2 reportEntry) {
        String creditorName =
                reportEntry.getNtryDtls().stream()
                        .findFirst()
                        .flatMap(x -> x.getTxDtls().stream().findFirst())
                        .map(EntryTransaction2::getRltdPties)
                        .map(TransactionParty2::getCdtr)
                        .map(PartyIdentification32::getNm)
                        .orElse(null);
        String debtorName =
                reportEntry.getNtryDtls().stream()
                        .findFirst()
                        .flatMap(x -> x.getTxDtls().stream().findFirst())
                        .map(EntryTransaction2::getRltdPties)
                        .map(TransactionParty2::getDbtr)
                        .map(PartyIdentification32::getNm)
                        .orElse(null);
        return isDebit(reportEntry) ? creditorName : debtorName;
    }
}
