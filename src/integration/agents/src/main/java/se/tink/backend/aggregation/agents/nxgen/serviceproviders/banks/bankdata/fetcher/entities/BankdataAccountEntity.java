package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankdataAccountEntity {

    public static final String REGISTRATION_NUMBER_TEMP_STORAGE_KEY = "regNo";
    public static final String ACCOUNT_NUMBER_TEMP_STORAGE_KEY = "accountNo";

    private static final String SAVINGS_ACCOUNT_NAME_PART = "Opsparing";

    private String regNo;
    private String accountNo;
    private double balance;
    private String bicSwift;
    private String currencyCode;
    private double drawingRight;
    private String iban;
    private String name;
    private Boolean transfersToAllowed;
    private Boolean transfersFromAllowed;
    private String accountOwner;
    private boolean ownAccount;
    private boolean mastercard;

    public AccountTypes getAccountType() {
        if (mastercard) {
            return AccountTypes.CREDIT_CARD;
        }
        if (drawingRight > 0) {
            return AccountTypes.LOAN;
        }
        if (StringUtils.containsIgnoreCase(name, SAVINGS_ACCOUNT_NAME_PART)) {
            return AccountTypes.SAVINGS;
        }
        return AccountTypes.CHECKING;
    }
}
