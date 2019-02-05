package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class AccountTransaction {
    private static final AggregationLogger LOGGER = new AggregationLogger(AccountTransaction.class);

    private String beneficiaire;
    @JsonProperty("idMouvement")
    private String transactionId;
    @JsonProperty("libelleMouvement")
    private String transactionLabel;
    @JsonProperty("libelle1ODC")
    private String secondTransactionLabel;
    private String libelle2ODC;
    @JsonProperty("dateOperation")
    private Date transactionDate;
    private String referenceMouvement;
    @JsonProperty("montant")
    private MontantEntity amount;
    private MontantEntity soldeEvolutif;
    private TypeEntity familleOperation;
    private CodeOperationEntity codeOperation;
    private long dateComptable;
    private long dateValeur;
    private TypeEntity typeOperation;
    @JsonProperty("statutMouvement")
    private TypeEntity transactionStatus;
    private boolean isOperationDuJour;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getTransactionDescription())
                .setDate(transactionDate)
                .setAmount(amount.toTinkAmount())
                .setExternalId(transactionId)
                .setPending(isPending())
                .build();
    }

    private String getTransactionDescription() {
        return BanquePopulaireConstants.Fetcher.CARD_TRANSACTION_DESCRIPTION_PATTERN.matcher(transactionLabel)
                .matches() ? secondTransactionLabel : transactionLabel;
    }

    private boolean isPending() {
        if (!BanquePopulaireConstants.Status.TRANSACTION_STATUS_MAPPER.containsKey(transactionStatus.getCode())) {
            LOGGER.info(BanquePopulaireConstants.LogTags.UNKNOWN_TRANSACTION_STATUS.toString() +
                    "  " +
                    SerializationUtils.serializeToString(transactionStatus));
        }
        return BanquePopulaireConstants.Status.TRANSACTION_STATUS_MAPPER
                .getOrDefault(transactionStatus.getCode(), false);
    }
}
