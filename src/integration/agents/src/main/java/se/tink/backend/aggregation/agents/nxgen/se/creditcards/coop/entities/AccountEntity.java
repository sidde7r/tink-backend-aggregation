package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AccountEntity {
    @JsonProperty("AccountType")
    private int accountType;

    @JsonProperty("Balance")
    private double balance;

    @JsonProperty("CanSetInternetAndForeignPayments")
    private boolean canSetInternetAndForeignPayments;

    @JsonProperty("CreditLimit")
    private int creditLimit;

    @JsonProperty("Details")
    private List<DetailsEntity> details;

    @JsonProperty("Name")
    private String accountName;

    @JsonProperty("TotalBalance")
    private double totalBalance;

    /**
     * To be able to easily do transfers to the account we can add the plusgiro identifier with OCR
     * that should be available on all coop accounts that you can pay to. There might be accounts
     * that are special, so we only add those with valid ocr and plusgiro.
     */
    private static List<AccountIdentifier> getAccountIdentifier(
            Map<String, String> accountDetailsMap) {
        String plusGiro = accountDetailsMap.get(CoopConstants.Account.PG_NUMBER);
        String ocr = accountDetailsMap.get(CoopConstants.Account.OCR_NUMBER);

        // Ensure we have values we expect from a valid destination account
        if (Strings.isNullOrEmpty(plusGiro) || Strings.isNullOrEmpty(ocr)) {
            return Collections.emptyList();
        }

        PlusGiroIdentifier plusGiroIdentifier = new PlusGiroIdentifier(plusGiro, ocr);
        if (!plusGiroIdentifier.isValid()) {
            return Collections.emptyList();
        } else {
            return Lists.newArrayList(plusGiroIdentifier);
        }
    }

    @JsonIgnore
    public boolean isCard() {
        return AccountTypes.CREDIT_CARD == guessAccountType();
    }

    public Optional<CreditCardAccount> toTinkCard(String credentialsId) {
        AccountTypes type = guessAccountType();
        if (type != AccountTypes.CREDIT_CARD) {
            return Optional.empty();
        }

        Map<String, String> accountDetailsMap = getAccountDetailsMap();
        String accountNumber = accountDetailsMap.get(CoopConstants.Account.ACCOUNT_NUMBER);
        if (Strings.isNullOrEmpty(accountNumber)) {
            return Optional.empty();
        }

        String ownerName = accountDetailsMap.get(CoopConstants.Account.OWNER_NAME);
        HolderName holdername = Strings.isNullOrEmpty(ownerName) ? null : new HolderName(ownerName);

        List<AccountIdentifier> identifiers = getAccountIdentifier(accountDetailsMap);

        CreditCardAccount card =
                CreditCardAccount.builder(getUniqueId(credentialsId))
                        .setName(accountName)
                        .setAccountNumber(accountNumber)
                        .setExactBalance(ExactCurrencyAmount.inSEK(totalBalance))
                        .setHolderName(holdername)
                        .setBankIdentifier(String.valueOf(this.accountType))
                        .setExactAvailableCredit(ExactCurrencyAmount.inSEK(balance))
                        .addIdentifiers(identifiers)
                        .build();

        return Optional.of(card);
    }

    public Optional<TransactionalAccount> toTinkAccount(String credentialsId) {
        AccountTypes type = guessAccountType();
        if (type == AccountTypes.CREDIT_CARD) {
            return Optional.empty();
        }

        Map<String, String> accountDetailsMap = getAccountDetailsMap();
        String accountNumber = accountDetailsMap.get(CoopConstants.Account.ACCOUNT_NUMBER);
        if (Strings.isNullOrEmpty(accountNumber)) {
            return Optional.empty();
        }

        String ownerName = accountDetailsMap.get(CoopConstants.Account.OWNER_NAME);
        HolderName holdername = Strings.isNullOrEmpty(ownerName) ? null : new HolderName(ownerName);

        List<AccountIdentifier> identifiers = getAccountIdentifier(accountDetailsMap);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.OTHER)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inSEK(totalBalance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueId(credentialsId))
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifiers(identifiers)
                                .build())
                .setApiIdentifier(String.valueOf(this.accountType))
                .addHolderName(holdername == null ? "" : holdername.toString())
                .build();
    }

    @JsonIgnore
    private Map<String, String> getAccountDetailsMap() {
        return Optional.ofNullable(details).orElseGet(Collections::emptyList).stream()
                .collect(Collectors.toMap(DetailsEntity::getIdLowerCase, DetailsEntity::getValue));
    }

    @JsonIgnore
    private String getUniqueId(String credentialsId) {
        // THIS IS TO AVOID Migration of unique ID until we find a way to handle Migrations in a
        // better way than today.
        //        return accountNumber;
        return hashLegacyBankId(credentialsId, coopAccountType());
    }

    /**
     * Preferrably we'd use the account number instead, but that'll require merging of data since V1
     * version of Coop used same kind of logic for hashing the BankId.
     */
    private String hashLegacyBankId(String credentialsId, CoopConstants.AccountType accountType) {
        return StringUtils.hashAsStringMD5(credentialsId + accountType.getLegacyBankIdPart());
    }

    @JsonIgnore
    private CoopConstants.AccountType coopAccountType() {
        CoopConstants.AccountType accountType = CoopConstants.AccountType.valueOf(this.accountType);
        if (accountType == null) {
            accountType = CoopConstants.AccountType.guessFromName(this.accountName);
        }

        return accountType;
    }

    @JsonIgnore
    private AccountTypes guessAccountType() {
        CoopConstants.AccountType accountType = coopAccountType();

        if (accountType == null) {
            return AccountTypes.OTHER;
        }

        switch (accountType) {
            case MEDMERA_MER:
            case MEDMERA_EFTER_1:
            case MEDMERA_EFTER_2:
            case MEDMERA_FORE:
            case MEDMERA_FAKTURA:
            case MEDMERA_VISA:
                return AccountTypes.CREDIT_CARD;
            case MEDMERA_KONTO:
            default:
                return AccountTypes.OTHER;
        }
    }
}
