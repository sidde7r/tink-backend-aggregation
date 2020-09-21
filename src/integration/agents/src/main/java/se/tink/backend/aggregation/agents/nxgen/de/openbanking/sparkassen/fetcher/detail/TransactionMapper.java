package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.EntryEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.TransactionDetailsEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapper {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(FetchTransactionsResponse.class);
        } catch (JAXBException e) {
            logger.error(ErrorMessages.COULD_NOT_INITIALIZE_JAXBCONTEXT);
            throw new IllegalStateException(ErrorMessages.COULD_NOT_INITIALIZE_JAXBCONTEXT);
        }
    }

    private static final String DBIT = "DBIT";
    private static final String BOOKED = "BOOK";

    public static Optional<FetchTransactionsResponse> tryParseXmlResponse(String xml) {
        try {
            Unmarshaller m = context.createUnmarshaller();
            return Optional.of((FetchTransactionsResponse) m.unmarshal(new StringReader(xml)));
        } catch (JAXBException e) {
            logger.error(ErrorMessages.COULD_NOT_PARSE_TRANSACTIONS);
        }
        return Optional.empty();
    }

    public static AggregationTransaction toTinkTransaction(EntryEntity entryEntity) {

        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        entryEntity.getAmount().getValue(), entryEntity.getAmount().getCurrency());

        return Transaction.builder()
                .setPending(!TransactionMapper.isBooked(entryEntity))
                .setAmount(isDebit(entryEntity) ? amount.negate() : amount)
                .setDescription(getDescription(entryEntity))
                .setDate(LocalDate.parse(entryEntity.getValueDate().getDate()))
                .build();
    }

    private static boolean isBooked(EntryEntity entryEntity) {
        return BOOKED.equalsIgnoreCase(entryEntity.getStatus());
    }

    private static boolean isDebit(EntryEntity entryEntity) {
        return DBIT.equalsIgnoreCase(entryEntity.getCreditDebitIndicator());
    }

    private static String getDescription(EntryEntity entryEntity) {
        TransactionDetailsEntity transactionDetails =
                entryEntity.getEntryDetails().getTransactionDetails();

        String beneficiary = null;
        if (isDebit(entryEntity)) {
            if (transactionDetails.getRelatedParties().getCreditor() != null) {
                beneficiary = transactionDetails.getRelatedParties().getCreditor().getName();
            }
        } else {
            if (transactionDetails.getRelatedParties().getDebtor() != null) {
                beneficiary = transactionDetails.getRelatedParties().getDebtor().getName();
            }
        }

        String purpose = transactionDetails.getRemittanceInformation().getUnstructured();

        if (beneficiary == null
                || beneficiary.isEmpty()
                || beneficiary.toLowerCase().contains("paypal")) {
            // PayPayl gets special treatment for now here in agent code, which isn't ideal.
            // ITE-1413 explains it a bit
            return purpose;
        } else {
            return beneficiary + " " + purpose;
        }
    }
}
