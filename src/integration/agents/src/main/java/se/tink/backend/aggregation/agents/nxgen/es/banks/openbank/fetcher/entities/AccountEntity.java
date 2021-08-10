package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.HolderNames;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Slf4j
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
        return ACCOUNT_TYPE_MAPPER.isOneOf(
                getAccountInfoNewFormat().getProductCode(),
                TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    private AccountTypes getTinkAccountType() {
        return ACCOUNT_TYPE_MAPPER
                .translate(getAccountInfoNewFormat().getProductCode())
                .orElse(AccountTypes.OTHER);
    }

    private String getAccountName() {
        final String contractNumber = getAccountInfoOldFormat().getContractNumber();

        // The bank app shows the account name as: "[DESCRIPTION]
        // ...[LAST_FOUR_DIGITS_OF_IBAN/CONTRACT_NUMBER]"
        return String.format(
                "%s ...%s",
                getDescription(), contractNumber.substring(contractNumber.length() - 4));
    }

    public Optional<TransactionalAccount> toTinkAccount(
            String iban, List<AccountHolderResponse> holders) {
        // Openbank account number is the last 10 digits of the IBAN
        final AccountInfoEntity accountInfoOldFormat = getAccountInfoOldFormat();
        final String accountNumber =
                accountInfoOldFormat.getProductCode() + accountInfoOldFormat.getContractNumber();
        final String accountName = getAccountName();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(getTinkAccountType()).get())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(balance.toTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber.toLowerCase())
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN, iban, accountName))
                                .build())
                .addParties(createParties(holders).asJava())
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

    private List<Party> createParties(List<AccountHolderResponse> holders) {
        if (holders.isEmpty()) {
            return List.of(new Party(getHolderName(), Role.HOLDER));
        }
        return holders.map(this::getHolderName);
    }

    private Party getHolderName(AccountHolderResponse accountHolder) {
        return Match(accountHolder.getInterventionType())
                .of(
                        Case($(HolderNames.OWNER), new Party(accountHolder.getName(), Role.HOLDER)),
                        Case(
                                $(HolderNames.AUTHORIZED),
                                new Party(accountHolder.getName(), Role.AUTHORIZED_USER)),
                        Case(
                                $(),
                                () -> {
                                    log.info(
                                            "Participant {} with association {}",
                                            accountHolder.getName(),
                                            accountHolder.getInterventionType());
                                    return new Party(accountHolder.getName(), Role.OTHER);
                                }));
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
