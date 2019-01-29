package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private static final AggregationLogger LOGGER = new AggregationLogger(AccountEntity.class);

    @JsonProperty("kontonummer")
    private String accountNumber;
    @JsonProperty("kontonummerGuid")
    private String accountId;
    @JsonProperty("isEier")
    private Boolean isOwner;
    @JsonProperty("hovedKontotype")
    private String mainAccountType;
    @JsonProperty("kontotype")
    private String accountType;
    @JsonProperty("kontoeierNavn")
    private String accountOwnerName;
    @JsonProperty("kontonavn")
    private String accountName;
    @JsonProperty("kontogruppe")
    private String accountGroup;
    @JsonProperty("tilgang")
    private AssetEntity asset;
    @JsonProperty("saldo")
    private Double balance;
    @JsonProperty("disponibelt")
    private Double disposable;
    @JsonProperty("harUttaksgebyr")
    private Boolean hasWithdrawalFee;
    @JsonProperty("isInnlaan")
    private Boolean isDepositAccount;
    private Boolean isSparX;
    @JsonProperty("isGjeldsbrevlaan")
    private Boolean isDebentureBonds;
    @JsonProperty("isFlexilaan")
    private Boolean isFlexiLoan;
    @JsonProperty("isFlexikreditt")
    private Boolean isFlexiCredit;
    @JsonProperty("isNedbetalingslaan")
    private Boolean isInstalmentLoan;
    @JsonProperty("isGaranti")
    private Boolean isGuarantee;
    @JsonProperty("isKredittkort")
    private Boolean isCreditCard;
    @JsonProperty("isByggelaan")
    private Boolean isConstructionLoan;
    @JsonProperty("isSkattetrekkskonto")
    private Boolean isTaxDeductionAccount;
    @JsonProperty("isValutalaan")
    private Boolean isCurrencyLoan;
    private Boolean isBsu;
    @JsonProperty("visningsnavn")
    private String displayName;

    public Boolean isDepositAccount() {
        return isDepositAccount
                && !isFlexiLoan
                && !isInstalmentLoan
                && !isCurrencyLoan
                && !isConstructionLoan
                && !isCreditCard
                && !isGuarantee
                && !isDebentureBonds
                && !isFlexiCredit;
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), accountNumber, Amount.inNOK(disposable))
                .setAccountNumber(accountNumber)
                .setName(accountNumber)
                .setBankIdentifier(accountId)
                .build();
    }

    private AccountTypes getTinkAccountType() {
        if (accountType == null) {
            return AccountTypes.OTHER;
        }

        switch(accountType.toLowerCase()) {
        case SparebankenVestConstants.AccountTypes.UNSPECIFIED:
        case SparebankenVestConstants.AccountTypes.TAX_DEDUCTION:
        case SparebankenVestConstants.AccountTypes.DEPOSITUM:
            return AccountTypes.OTHER;
        default:
            LOGGER.warn(String.format(
                    "Could not map account type [%s] to a Tink account type", accountType));
            return AccountTypes.OTHER;
        }
    }
}
