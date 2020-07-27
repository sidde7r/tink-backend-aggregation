package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import static se.tink.backend.agents.rpc.AccountTypes.OTHER;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AgreementsListEntity {
    @JsonProperty("saldoLimite")
    private String balanceLimit;

    @JsonProperty("relacionAcuerdoPersona")
    private String relationshipAgreementPerson;

    private String sitIrregular;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("aliasBE")
    private String aliasbe;

    @JsonProperty("saldoNoDispuesto")
    private String balanceNoUsed;

    @JsonProperty("campoLineaGrupo")
    private String fieldLineGroup;

    @JsonProperty("codigoMoneda")
    private String currencyCode;

    @JsonProperty("saldoDeudaImpg")
    private String debtBalanceImpg;

    @JsonProperty("acuerdo")
    private String agreement;

    @JsonProperty("numFavoritas")
    private String numFavorites;

    @JsonProperty("acuerdoRelacionado")
    private String relatedAgreement;

    @JsonProperty("saldoNoVencido")
    private String unspentBalance;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("DatosTarjeta")
    private CardDataEntityGlobalPositionResponse cardData;

    public boolean isAccount() {
        return accountType != null;
    }

    public boolean isCard() {
        return cardData != null;
    }

    public Optional<TransactionalAccount> toTinkAccount(String holderName) {
        AccountTypes type =
                EvoBancoConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType + "#" + aliasbe)
                        .orElse(OTHER);

        Optional<TransactionalAccount> tinkAccount;

        Amount balance = Amount.inEUR(AgentParsingUtils.parseAmount(unspentBalance));

        switch (type) {
            case CHECKING:
                tinkAccount =
                        Optional.of(
                                CheckingAccount.builder()
                                        .setUniqueIdentifier(iban)
                                        .setAccountNumber(iban)
                                        .setBalance(balance)
                                        .setAlias(aliasbe)
                                        .addAccountIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifier.Type.IBAN, iban))
                                        .setApiIdentifier(agreement)
                                        .addHolderName(holderName)
                                        .setProductName(accountType)
                                        .build());

                break;

            case SAVINGS:
                tinkAccount =
                        Optional.of(
                                SavingsAccount.builder()
                                        .setUniqueIdentifier(iban)
                                        .setAccountNumber(iban)
                                        .setBalance(balance)
                                        .setAlias(aliasbe)
                                        .addAccountIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifier.Type.IBAN, iban))
                                        .setApiIdentifier(agreement)
                                        .addHolderName(holderName)
                                        .setProductName(accountType)
                                        .build());

                break;

            case OTHER:
            default:
                tinkAccount = Optional.empty();
                break;
        }

        return tinkAccount;
    }

    public Optional<CreditCardAccount> toTinkCreditCard() {
        ExactCurrencyAmount balance =
                ExactCurrencyAmount.of(
                        AgentParsingUtils.parseAmount(cardData.getCreditUsed()), "EUR");

        ExactCurrencyAmount availableCredit =
                ExactCurrencyAmount.of(
                        AgentParsingUtils.parseAmount(cardData.getCreditAvailableBalance()), "EUR");

        return Optional.of(
                CreditCardAccount.builderFromFullNumber(cardData.getPanToken(), aliasbe)
                        .setExactBalance(balance)
                        .setExactAvailableCredit(availableCredit)
                        .setHolderName(new HolderName(cardData.getHolder()))
                        .setBankIdentifier(cardData.getPanToken())
                        .putInTemporaryStorage(Storage.CARD_STATE, cardData.getStateDescription())
                        .build());
    }
}
