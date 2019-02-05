package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.agents.rpc.AccountTypes;

@JsonObject
public class CardEntity {
    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("listable")
    private boolean listable;

    @JsonProperty("nombretitular")
    private String holderName;

    @JsonProperty("saldoDispuesto")
    private AmountEntity balanceUsed;

    @JsonProperty("saldoDisponible")
    private AmountEntity balanceAvailable;

    @JsonProperty("limite")
    private AmountEntity creditLimit;

    @JsonProperty("panmdp")
    private String cardNumber;

    @JsonProperty("bloqueada")
    private boolean isBlocked;

    @JsonProperty("cuentaAsociada")
    private IbanEntity associatedAccount;

    @JsonProperty("indstrj")
    private String indstrj;

    @JsonProperty("codigosError")
    private ErrorCodeEntity errorCodes;

    @JsonProperty("tipoTarjeta")
    private String cardType;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("contrato")
    private ContractEntity contract;

    @JsonProperty("indicadorAcceso")
    private String accessIndicator;

    @JsonProperty("fecsittarj")
    private String fecsittarj;

    @JsonProperty("activable")
    private boolean isActive;

    @JsonProperty("tipoa")
    private String typeCode;

    @JsonProperty("filtros")
    private FilterEntity filter;

    public String getDescription() {
        return description;
    }

    public boolean isListable() {
        return listable;
    }

    public String getHolderName() {
        return holderName;
    }

    public AmountEntity getBalanceUsed() {
        return balanceUsed;
    }

    public AmountEntity getBalanceAvailable() {
        return balanceAvailable;
    }

    public AmountEntity getCreditLimit() {
        return creditLimit;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public IbanEntity getAssociatedAccount() {
        return associatedAccount;
    }

    public String getIndstrj() {
        return indstrj;
    }

    public ErrorCodeEntity getErrorCodes() {
        return errorCodes;
    }

    public String getCardType() {
        return cardType;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public String getAccessIndicator() {
        return accessIndicator;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public FilterEntity getFilter() {
        return filter;
    }

    public boolean isCreditCardAccount() {
        return OpenbankConstants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(contract.getProductCode());
    }

    private AccountTypes getTinkAccountType() {
        Optional<AccountTypes> accountType =
                OpenbankConstants.ACCOUNT_TYPE_MAPPER.translate(contract.getProductCode());
        return accountType.orElse(AccountTypes.OTHER);
    }

    public CreditCardAccount toTinkAccount() {
        String accountNumber = contract.getProductCode() + contract.getContractNumber();

        return CreditCardAccount.builderFromFullNumber(cardNumber, description)
                .setAccountNumber(accountNumber)
                .setName(description)
                .setBalance(getBalanceUsed().toTinkAmount())
                .setAvailableCredit(getBalanceAvailable().toTinkAmount())
                .setBankIdentifier(cardNumber)
                .putInTemporaryStorage(
                        OpenbankConstants.Storage.PRODUCT_CODE_NEW, contract.getProductCode())
                .putInTemporaryStorage(
                        OpenbankConstants.Storage.CONTRACT_NUMBER_NEW, contract.getContractNumber())
                .putInTemporaryStorage(OpenbankConstants.Storage.CARD_NUMBER, cardNumber)
                .build();
    }
}
