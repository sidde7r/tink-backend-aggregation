package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;

@JsonObject
@Slf4j
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountEntity {

    @JsonIgnore private final BalanceHelper balanceHelper = new BalanceHelper();
    @JsonIgnore private final IdentifiersProvider identifiersProvider = new IdentifiersProvider();

    @Getter
    @JsonProperty("_id")
    private String id;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private String accountName;
    private List<AccountNumberEntity> accountNumbers;
    @Getter private String accountType;
    private BigDecimal availableBalance;
    private BankEntity bank;
    private BigDecimal bookedBalance;
    private String country;
    private String creditLimit;
    private String currency;
    private String latestTransactionBookingDate;
    @Getter private String product;
    private String status;
    private String valueDatedBalance;

    public Optional<TransactionalAccount> toTinkAccount() {
        AccountIdentifier ibanIdentifier = new IbanIdentifier(getIban());
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                        accountType,
                        TransactionalAccountType.CHECKING)
                .withBalance(balanceHelper.buildBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifiersProvider.getUniqueIdentifier())
                                .withAccountNumber(ibanIdentifier.getIdentifier())
                                .withAccountName(
                                        Optional.ofNullable(product)
                                                .orElseThrow(
                                                        () ->
                                                                new NoSuchElementException(
                                                                        "Product is missing")))
                                .addIdentifiers(
                                        identifiersProvider.getIdentifiers(
                                                ibanIdentifier.getIdentifier()))
                                .build())
                .putInTemporaryStorage(NordeaBaseConstants.StorageKeys.ACCOUNT_ID, id)
                .setApiIdentifier(id)
                .addHolderName(accountName)
                .build();
    }

    public String getIban() {
        return ListUtils.emptyIfNull(accountNumbers).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.IBAN))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElseThrow(
                        () -> {
                            log.info(
                                    "Failed to fetch iban "
                                            + LogTag.from("openbanking_base_nordea"));
                            return new IllegalArgumentException();
                        });
    }

    private class BalanceHelper {
        private BalanceModule buildBalanceModule() {
            BalanceBuilderStep builder =
                    BalanceModule.builder()
                            .withBalance(getBookedBalance())
                            .setAvailableBalance(getAvailableBalance());
            getCreditLimit().ifPresent(builder::setCreditLimit);
            return builder.build();
        }

        private ExactCurrencyAmount getBookedBalance() {
            return new ExactCurrencyAmount(bookedBalance, currency);
        }

        public ExactCurrencyAmount getAvailableBalance() {
            return new ExactCurrencyAmount(availableBalance, currency);
        }

        private Optional<ExactCurrencyAmount> getCreditLimit() {
            return Optional.ofNullable(creditLimit).map(cl -> ExactCurrencyAmount.of(cl, currency));
        }
    }

    private class IdentifiersProvider {
        private String getUniqueIdentifier() {
            if (MarketCode.DK.name().equalsIgnoreCase(country)) {
                return extractDanishAccountNumberFromIban();
            }
            return getIban();
        }

        private String extractDanishAccountNumberFromIban() {
            return StringUtils.right(
                    getIban(), NordeaBaseConstants.TransactionalAccounts.DANISH_ACCOUNT_NO_LENGTH);
        }

        private List<AccountIdentifier> getIdentifiers(String iban) {
            List<AccountIdentifier> identifiers = new ArrayList<>();
            identifiers.add(new IbanIdentifier(bank.getBic(), iban));
            if (StringUtils.isNotBlank(getBban())) {
                identifiers.add(new BbanIdentifier(getBban()));
            }
            return identifiers;
        }

        private String getBban() {
            return ListUtils.emptyIfNull(accountNumbers).stream()
                    .filter(
                            acc ->
                                    acc.getType()
                                            .contains(
                                                    NordeaBaseConstants.AccountTypesResponse.BBAN))
                    .findFirst()
                    .map(AccountNumberEntity::getValue)
                    .orElse(null);
        }
    }

    // THE FOLLOWING METHODS ARE USED BY NORDEA SWEDEN

    @JsonIgnore
    public String getHolderName() {
        return formatHolderName();
    }

    @JsonIgnore
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier identifier = getAccountIdentifier();
        if (identifier.is(AccountIdentifierType.SE_NDA_SSN)) {
            return identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return identifier;
        }
    }

    @JsonIgnore
    public AccountIdentifier getAccountIdentifier() {
        if (NordeaBaseConstants.TransactionalAccounts.PERSONAL_ACCOUNT.equalsIgnoreCase(product)) {
            AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(AccountIdentifierType.SE_NDA_SSN, getSwedishBban());
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        if (NordeaBaseConstants.TransactionalAccounts.BUSINESS_ACCOUNT.equalsIgnoreCase(product)) {
            AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(AccountIdentifierType.SE_PG, getPlusgiro());
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        return AccountIdentifier.create(AccountIdentifierType.SE, getSwedishBban());
    }

    private String getSwedishBban() {
        return ListUtils.emptyIfNull(accountNumbers).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.BBAN_SE))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElse(getIban());
    }

    private String getPlusgiro() {
        return ListUtils.emptyIfNull(accountNumbers).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.PGNR))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElse(getIban());
    }

    public String getLast4Bban() {
        return getSwedishBban().substring(getSwedishBban().length() - 4);
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return balanceHelper.getAvailableBalance();
    }

    @JsonIgnore
    private String formatHolderName() {
        // We get accountName on the form "<SURNAME>,<FORENAME>", at least for Nordea Sweden.
        // Splitting on comma and changing the order of forename and surname.
        List<String> holderName =
                Stream.of(accountName.split(",")).map(String::trim).collect(Collectors.toList());
        return holderName.get(1) + " " + holderName.get(0);
    }

    public String getAccountName() {
        return accountName;
    }
}
