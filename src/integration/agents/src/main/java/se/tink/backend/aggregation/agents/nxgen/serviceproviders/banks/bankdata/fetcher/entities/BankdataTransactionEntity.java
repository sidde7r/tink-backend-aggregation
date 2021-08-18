package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankdataTransactionEntity {

    private BankdataAccountIdEntity accountId;
    private List<Map<String, Object>> splits;
    private String identifier;
    private String currency;
    private double balance;
    private boolean unCleared;
    private boolean flagged;
    private boolean balanced;
    private boolean message;
    private boolean attachment;
    private List<Map<String, Object>> tags;
    private String text;
    private double mainAmount;
    private String valeurDate;
    private String transactionDate;
    private boolean stoppable;
    private double newMaxSplitAmount;
    private int parentCategoryId;
    private boolean split;
    private boolean partial;
    private boolean outOfSyncTransaction;
    private int status;
    private boolean canVerify;
    private boolean reservation;
}
