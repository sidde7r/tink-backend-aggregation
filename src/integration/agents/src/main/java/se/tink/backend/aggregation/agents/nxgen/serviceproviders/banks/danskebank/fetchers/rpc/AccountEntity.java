package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@JsonObject
@Getter
public class AccountEntity {

    private String showCategory;
    private int sortValue;
    private boolean isFixedTermDeposit;
    private boolean isInLimitGroup;
    private boolean isSavingGoalAccountProduct;
    private boolean isBreadcrumbAccountProduct;
    private boolean isLoanAccount;
    private String invIdOwner;
    private String mandateAccMk;
    private boolean showAvailable;
    private boolean accessToCredit;
    private boolean accessToDebit;
    private boolean accessToQuery;
    private String currency;
    private String cardType;
    private String accountType;
    private String accountName;
    private String accountProduct;
    private String accountRegNoExt;
    private String accountNoExt;
    private String accountNoInt;
    private String languageCode;
    private double balanceAvailable;
    private double balance;
}
