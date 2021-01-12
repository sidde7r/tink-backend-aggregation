package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
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

    public boolean isCard() {
        return cardData != null;
    }

    private String getAccountTypeKey() {
        return accountType + "#" + aliasbe;
    }

    public Optional<TransactionalAccount> toTinkAccount(String holderName) {
        return EvoBancoConstants.ACCOUNT_TYPE_MAPPER
                .translate(getAccountTypeKey())
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
                                                                        AccountIdentifier.Type.IBAN,
                                                                        iban))
                                                        .setProductName(accountType)
                                                        .build())
                                        .addHolders(Holder.of(holderName, Role.HOLDER))
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
