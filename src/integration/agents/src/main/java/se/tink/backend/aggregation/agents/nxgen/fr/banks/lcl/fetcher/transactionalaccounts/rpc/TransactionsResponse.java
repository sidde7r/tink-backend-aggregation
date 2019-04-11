package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AgreementListEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.OpNcIEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {
    @JsonProperty("opNC_I")
    private OpNcIEntity opNcI;

    @JsonProperty("releveMoisActuelPasse")
    private boolean passesUnderCurrentMonth;

    @JsonProperty("lesTypesEpargne")
    private List<String> savingsTypes;

    @JsonProperty("listeMvt")
    private List<TransactionEntity> transactionsList;

    private boolean isOpNCBlooped;

    @JsonProperty("formatEnteteConcatene")
    private String concatenatedAgreementFormat;

    @JsonProperty("natureCompte")
    private String accountType;

    @JsonProperty("degradeCompteAbsent")
    private String degradedAbsentAccount;

    @JsonProperty("listeEntete")
    private AgreementListEntity agreementList;

    @JsonProperty("dateDernierJourOuvre")
    private String lastWorkingDayDate;

    @JsonProperty("moisActuel")
    private int currentMonth;

    @JsonProperty("formatPaginDB2Concatene")
    private String concatenatedPageDb2Format;

    public OpNcIEntity getOpNcI() {
        return opNcI;
    }

    public boolean isPassesUnderCurrentMonth() {
        return passesUnderCurrentMonth;
    }

    public List<String> getSavingsTypes() {
        return savingsTypes;
    }

    public List<TransactionEntity> getTransactionsList() {
        return transactionsList;
    }

    public boolean isOpNCBlooped() {
        return isOpNCBlooped;
    }

    public String getConcatenatedAgreementFormat() {
        return concatenatedAgreementFormat;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getDegradedAbsentAccount() {
        return degradedAbsentAccount;
    }

    public AgreementListEntity getAgreementList() {
        return agreementList;
    }

    public String getLastWorkingDayDate() {
        return lastWorkingDayDate;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public String getConcatenatedPageDb2Format() {
        return concatenatedPageDb2Format;
    }
}
