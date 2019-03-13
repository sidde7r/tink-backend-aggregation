package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.ACCOUNT_TYPE_MAPPER;

@JsonObject
public class AccountEntity {
    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("cnuevo")
    private AccountInfoEntity accountInfoNewFormat;

    @JsonProperty("cviejo")
    private AccountInfoEntity accountInfoOldFormat;

    @JsonProperty("codiban")
    private IbanEntity ibanEntity;

    @JsonProperty("nombretitular")
    private String holderName;

    @JsonProperty("fecimp")
    private String fecimp;

    @JsonProperty("catalogData")
    private CatalogDataEntity catalogData;

    @JsonProperty("saldoActual")
    private AmountEntity balance;

    @JsonProperty("saldosCuenta")
    private BalanceAccount balanceAccount;

    @JsonProperty("saldoContNatural")
    private AmountEntity balanceContNatural; // Not sure how to translsate this

    @JsonProperty("limite")
    private AmountEntity limit; // Credit limit?

    @JsonProperty("codigoclasefamilia")
    private String familyClassCode;

    @JsonProperty("codigosError")
    private ErrorCodeEntity errorCodes;

    @JsonProperty("criterios")
    private Object criteria;

    @JsonProperty("fechaapertura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fechaapertura;

    @JsonProperty("indicadorAcceso")
    private String indicadorAcceso;

    @JsonProperty("codigofamiliaproductos")
    private String productsFamilyCode;

    @JsonProperty("tipoCuenta") // Not actual account type since same for checking/saving
    private String accountType; // Check accountInfoOldFormat/accountInfoNewFormat instead

    @JsonProperty("codigosubfamiliaproductos")
    private String productsSubfamilyCode;

    @JsonProperty("filtros")
    private FilterEntity filter;

    @JsonProperty("isRoboAccount")
    private boolean isRoboAccount;

    public boolean isTransactionalAccount() {
        return ACCOUNT_TYPE_MAPPER.isTransactionalAccount(
                getAccountInfoNewFormat().getProductCode());
    }

    private AccountTypes getTinkAccountType() {
        return ACCOUNT_TYPE_MAPPER
                .translate(getAccountInfoNewFormat().getProductCode())
                .orElse(AccountTypes.OTHER);
    }

    private String getIban() {
        return ibanEntity.getIban();
    }

    private String getAccountName() {
        String contractNumber = getAccountInfoOldFormat().getContractNumber();

        // The bank app shows the account name as: "[DESCRIPTION]
        // ...[LAST_FOUR_DIGITS_OF_IBAN/CONTRACT_NUMBER]"
        return String.format(
                "%s ...%s",
                getDescription(), contractNumber.substring(contractNumber.length() - 4));
    }

    public TransactionalAccount toTinkAccount() {
        // Openbank account number is the last 10 digits of the IBAN
        AccountInfoEntity accountInfoOldFormat = getAccountInfoOldFormat();
        String accountNumber =
                accountInfoOldFormat.getProductCode() + accountInfoOldFormat.getContractNumber();

        return TransactionalAccount.builder(getTinkAccountType(), accountNumber.toLowerCase())
                .setAccountNumber(accountNumber)
                .setName(getAccountName())
                .setBalance(balance.toTinkAmount())
                .setBankIdentifier(accountNumber)
                .putInTemporaryStorage(
                        OpenbankConstants.Storage.PRODUCT_CODE_OLD,
                        accountInfoOldFormat.getProductCode())
                .putInTemporaryStorage(
                        OpenbankConstants.Storage.CONTRACT_NUMBER_OLD,
                        accountInfoOldFormat.getContractNumber())
                .putInTemporaryStorage(
                        OpenbankConstants.Storage.PRODUCT_CODE_NEW,
                        accountInfoOldFormat.getProductCode())
                .putInTemporaryStorage(
                        OpenbankConstants.Storage.CONTRACT_NUMBER_NEW,
                        accountInfoOldFormat.getContractNumber())
                .build();
    }

    public String getDescription() {
        return description;
    }

    public AccountInfoEntity getAccountInfoNewFormat() {
        return accountInfoNewFormat;
    }

    public AccountInfoEntity getAccountInfoOldFormat() {
        return accountInfoOldFormat;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getFecimp() {
        return fecimp;
    }

    public CatalogDataEntity getCatalogData() {
        return catalogData;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getFamilyClassCode() {
        return familyClassCode;
    }

    public BalanceAccount getBalanceAccount() {
        return balanceAccount;
    }

    public ErrorCodeEntity getErrorCodes() {
        return errorCodes;
    }

    public Date getFechaapertura() {
        return fechaapertura;
    }

    public String getIndicadorAcceso() {
        return indicadorAcceso;
    }

    public String getProductsFamilyCode() {
        return productsFamilyCode;
    }

    public AmountEntity getBalanceContNatural() {
        return balanceContNatural;
    }

    public AmountEntity getLimit() {
        return limit;
    }

    public String getAccountType() {
        return accountType;
    }

    public Object getProductsSubfamilyCode() {
        return productsSubfamilyCode;
    }

    public FilterEntity getFilter() {
        return filter;
    }

    public boolean isIsRoboAccount() {
        return isRoboAccount;
    }
}
