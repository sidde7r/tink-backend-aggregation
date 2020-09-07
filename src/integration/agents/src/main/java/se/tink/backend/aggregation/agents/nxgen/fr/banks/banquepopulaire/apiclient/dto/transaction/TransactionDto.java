package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.TypeDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class TransactionDto {

    @JsonProperty("beneficiaire")
    private String beneficiary;

    @JsonProperty("idMouvement")
    private String transactionId;

    @JsonProperty("libelleMouvement")
    private String transactionLabel;

    @JsonProperty("libelle1ODC")
    private String secondTransactionLabel;

    private String libelle2ODC;

    @JsonProperty("dateOperation")
    private long transactionTimestamp;

    private String referenceMouvement;

    @JsonProperty("montant")
    private BalanceDto balance;

    private BalanceDto soldeEvolutif;

    private TypeDto familleOperation;

    private CodeOperationDto codeOperation;

    private long dateComptable;

    private long dateValeur;

    private TypeDto typeOperation;

    @JsonProperty("statutMouvement")
    private TypeDto transactionStatus;

    private boolean isOperationDuJour;
}
