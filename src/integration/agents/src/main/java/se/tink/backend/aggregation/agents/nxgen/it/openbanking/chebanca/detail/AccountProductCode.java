package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.libraries.account.enums.AccountTypes;

public enum AccountProductCode {
    CREDIT_CARD_1("CAPC", AccountTypes.CREDIT_CARD),
    CREDIT_CARD_2("CA30", AccountTypes.CREDIT_CARD),
    DEPOSIT("CDEP", AccountTypes.SAVINGS),
    BUSINESS_DEPOSIT("IMPB", AccountTypes.SAVINGS),
    CHECKING_1("MPAC", AccountTypes.CHECKING),
    CHECKING_2("MPAZ", AccountTypes.CHECKING),
    CHECKING_3("MPBC", AccountTypes.CHECKING),
    CHECKING_4("MPBL", AccountTypes.CHECKING),
    CHECKING_5("MPFC", AccountTypes.CHECKING),
    CHECKING_6("MPFU", AccountTypes.CHECKING),
    CHECKING_7("MPGC", AccountTypes.CHECKING),
    CHECKING_8("MPGI", AccountTypes.CHECKING),
    CHECKING_9("MPGR", AccountTypes.CHECKING),
    CHECKING_10("MPRC", AccountTypes.CHECKING),
    CHECKING_11("MPRO", AccountTypes.CHECKING),
    CHECKING_12("MPVC", AccountTypes.CHECKING),
    CHECKING_13("MPVE", AccountTypes.CHECKING),
    CHECKING_14("MPYC", AccountTypes.CHECKING),
    CHECKING_15("MP0C", AccountTypes.CHECKING),
    CHECKING_16("MP00", AccountTypes.CHECKING),
    CHECKING_17("AZIM", AccountTypes.CHECKING),
    CHECKING_18("0600", AccountTypes.CHECKING),
    CHECKING_19("0601", AccountTypes.CHECKING),
    CHECKING_20("0603", AccountTypes.CHECKING),
    CHECKING_21("0604", AccountTypes.CHECKING),
    CHECKING_22("0608", AccountTypes.CHECKING),
    CHECKING_23("0631", AccountTypes.CHECKING),
    CHECKING_24("0632", AccountTypes.CHECKING),
    CHECKING_25("0633", AccountTypes.CHECKING),
    CHECKING_26("0641", AccountTypes.CHECKING),
    CHECKING_27("0642", AccountTypes.CHECKING),
    CHECKING_28("0643", AccountTypes.CHECKING),
    CHECKING_29("0644", AccountTypes.CHECKING),
    CHECKING_30("0645", AccountTypes.CHECKING),
    CHECKING_31("0646", AccountTypes.CHECKING),
    CHECKING_32("0647", AccountTypes.CHECKING),
    CHECKING_33("0648", AccountTypes.CHECKING),
    CHECKING_34("0649", AccountTypes.CHECKING),
    CHECKING_35("0650", AccountTypes.CHECKING),
    CHECKING_36("0651", AccountTypes.CHECKING),
    CHECKING_37("0652", AccountTypes.CHECKING),
    CHECKING_38("0653", AccountTypes.CHECKING),
    CHECKING_39("0654", AccountTypes.CHECKING),
    CHECKING_40("0655", AccountTypes.CHECKING),
    CHECKING_41("0656", AccountTypes.CHECKING),
    CHECKING_42("0657", AccountTypes.CHECKING),
    CHECKING_43("0658", AccountTypes.CHECKING),
    CHECKING_44("0665", AccountTypes.CHECKING),
    CHECKING_45("0666", AccountTypes.CHECKING),
    CHECKING_46("0694", AccountTypes.CHECKING),
    CHECKING_47("0695", AccountTypes.CHECKING),
    CHECKING_48("0697", AccountTypes.CHECKING),
    CHECKING_49("0698", AccountTypes.CHECKING);

    private String code;
    private AccountTypes accountType;

    AccountProductCode(String code, AccountTypes accountType) {
        this.code = code;
        this.accountType = accountType;
    }

    public String getCode() {
        return code;
    }

    public AccountTypes getAccountType() {
        return accountType;
    }

    public static AccountProductCode ofCode(String productCode) {
        return Stream.of(AccountProductCode.values())
                .filter(prdCode -> Objects.equals(prdCode.code, productCode))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "No AccountProductCode that matches code: %s",
                                                productCode)));
    }

    public static boolean isCheckingAccount(AccountEntity accountEntity) {
        return isAccountOfGivenType(accountEntity, AccountTypes.CHECKING);
    }

    public static boolean isSavingsAccount(AccountEntity accountEntity) {
        return isAccountOfGivenType(accountEntity, AccountTypes.SAVINGS);
    }

    public static boolean isCreditCardAccount(AccountEntity accountEntity) {
        return isAccountOfGivenType(accountEntity, AccountTypes.CREDIT_CARD);
    }

    private static boolean isAccountOfGivenType(
            AccountEntity accountEntity, AccountTypes creditCard) {
        List<AccountProductCode> codesOfInterest = getProductCodesOfType(creditCard);
        try {
            AccountProductCode productCode =
                    AccountProductCode.ofCode(accountEntity.getProduct().getCode());
            return codesOfInterest.stream().anyMatch(code -> code == productCode);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static List<AccountProductCode> getProductCodesOfType(AccountTypes type) {
        return Stream.of(AccountProductCode.values())
                .filter(s -> s.getAccountType() == type)
                .collect(Collectors.toList());
    }
}
