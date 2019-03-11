package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class DataItemEntity {
    private TransactionTypeEntity transactionType;
    private String operationDate;
    private AdditionalInformationEntity additionalInformation;
    private String valuationDate;
    private String concept;
    private ContractEntity contract;
    private MoneyFlowEntity moneyFlow;
    private String id;
    private String accountedDate;
    private FinancingTypeEntity financingType;
    private LocalAmountEntity localAmount;
    private StatusEntity status;

    @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(DataItemEntity.class);

    private Date getDate() {
        try {
            return BbvaMxConstants.DATE.TRANSACTION_DATE_FORAMT.parse(operationDate.substring(0, 23));
        } catch (ParseException e) {
            logger.error("{} {}", BbvaMxConstants.LOGGING.DATE_PARSING_ERROR, e.toString());
            throw new IllegalStateException("Date is invalid");
        }
    }

    // Concept + AdditionalInformation.AdditionalData
    private String getDescription() {
        StringBuilder builder = new StringBuilder();

        if (!Strings.isNullOrEmpty(concept)) {
            builder.append(concept.trim());
        }

        if (additionalInformation != null
                && Strings.isNullOrEmpty(additionalInformation.getAdditionalData())) {
            builder.append("" + additionalInformation.getAdditionalData().trim());
        }

        return builder.toString();
    }

    public Optional<Transaction> toTinkTransaction() {
        try {
            return Optional.of(
                    Transaction.builder()
                            .setAmount(localAmount.toTinkAmount())
                            .setDate(getDate())
                            .setDescription(getDescription())
                            .build());
        } catch (Exception e) {
            logger.error("{} {}", BbvaMxConstants.LOGGING.TRANSACTION_PARSING_ERROR, e.toString());
            return Optional.empty();
        }
    }
}
