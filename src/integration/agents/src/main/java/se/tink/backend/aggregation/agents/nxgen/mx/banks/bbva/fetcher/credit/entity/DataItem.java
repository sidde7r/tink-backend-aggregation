package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class DataItem {
    private TransactionType transactionType;
    private String operationDate;
    private AdditionalInformation additionalInformation;
    private String valuationDate;
    private String concept;
    private CreditContract contract;
    private MoneyFlow moneyFlow;
    private String id;
    private String accountedDate;
    private FinancingType financingType;
    private LocalAmount localAmount;
    private Status status;

    @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(DataItem.class);

    @JsonIgnore
    private final SimpleDateFormat transactionDateForamt =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private Date getDate() {
        try {
            return transactionDateForamt.parse(operationDate.substring(0, 23));
        } catch (ParseException e) {
            logger.error("{} {}", BbvaMxConstants.LOGGING.DATE_PARSING_ERROR, e.toString(), e);
            throw new IllegalStateException("Date is invalid");
        }
    }

    public Optional<Transaction> toTinkCreditTransaction() {
        try {
            return Optional.of(
                    Transaction.builder()
                            .setDescription(concept)
                            .setDate(getDate())
                            .setAmount(localAmount.toTinkAmount())
                            .build());
        } catch (RuntimeException e) {
            logger.error(
                    "{} {}",
                    BbvaMxConstants.LOGGING.CREDIT_TRANSACTION_PARSING_ERROR,
                    e.toString(),
                    e);
            return Optional.empty();
        }
    }
}
