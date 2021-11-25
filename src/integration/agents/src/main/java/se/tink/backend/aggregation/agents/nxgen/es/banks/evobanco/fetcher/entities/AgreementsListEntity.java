package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
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

    public boolean isCreditCard() {
        return cardData != null && creditCardCondition();
    }

    private boolean creditCardCondition() {
        return new BigDecimal(cardData.getCardCreditLimit()).compareTo(BigDecimal.ZERO) > 0
                || new BigDecimal(cardData.getCreditCardDayATMLimit()).compareTo(BigDecimal.ZERO)
                        > 0;
    }

    public Optional<TransactionalAccount> toTinkAccount(String holderName) {
        String panForDebitCardAccount =
                cardData != null && cardData.getPanToken() != null ? cardData.getPanToken() : "";
        return EvoBancoConstants.ACCOUNT_TYPE_MAPPER
                .translate(aliasbe)
                .flatMap(
                        type ->
                                TransactionalAccount.nxBuilder()
                                        .withType(type)
                                        .withInferredAccountFlags()
                                        .withBalance(getBalance())
                                        .withId(
                                                IdModule.builder()
                                                        .withUniqueIdentifier(iban)
                                                        .withAccountNumber(iban)
                                                        .withAccountName(aliasbe)
                                                        .addIdentifier(
                                                                AccountIdentifier.create(
                                                                        AccountIdentifierType.IBAN,
                                                                        iban))
                                                        .setProductName(accountType)
                                                        .build())
                                        .addParties(new Party(holderName, Party.Role.HOLDER))
                                        .putInTemporaryStorage(
                                                Storage.PAN_TOKEN, panForDebitCardAccount)
                                        .setApiIdentifier(agreement)
                                        .build());
    }

    private BalanceModule getBalance() {
        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(
                                AgentParsingUtils.parseAmount(unspentBalance), "EUR"))
                .build();
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
