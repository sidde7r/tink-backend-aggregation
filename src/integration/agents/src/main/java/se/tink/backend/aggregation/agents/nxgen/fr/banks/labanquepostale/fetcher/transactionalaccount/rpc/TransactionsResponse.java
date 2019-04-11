package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {

    @JsonProperty("code_ret")
    private String codeRet;

    @JsonProperty("code_media")
    private String codeMedia;

    private String message;

    @JsonProperty("compteNumero")
    private String compteNumber;

    @JsonProperty("libelleNatureCompte")
    private String libelleNatureAccount;

    @JsonProperty("typePartenaire")
    private String partnerType;

    private String devise;

    @JsonProperty("avoirDispo")
    private double haveDispo;

    @JsonProperty("heureSoldeOp")
    private String hourBalanceOp;

    private int nbOperations;

    @JsonProperty("operations")
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions != null ? transactions : Collections.emptyList();
    }
}
