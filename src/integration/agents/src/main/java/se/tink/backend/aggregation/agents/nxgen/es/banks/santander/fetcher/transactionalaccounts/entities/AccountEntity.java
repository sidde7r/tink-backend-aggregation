package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.CustomerData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
@XmlRootElement
public class AccountEntity {
    private static final AggregationLogger log = new AggregationLogger(AccountEntity.class);

    @JsonProperty("comunes")
    private GeneralInfoEntity generalInfo;

    @JsonProperty("impSaldoActual")
    private AmountEntity balance;

    @JsonProperty("importeDispAut")
    private AmountEntity disposible;

    @JsonProperty("importeDispSinAut")
    private AmountEntity disposibleExclAut;

    @JsonProperty("importeLimite")
    private AmountEntity amountLimit;

    @JsonProperty("IBAN")
    private String iban;

    @JsonProperty("contratoIDViejo")
    private ContractEntity originalContractId;

    @JsonProperty("titular")
    private CustomerData customerData;

    @JsonProperty("tipoSituacionCto")
    private String creditTypeSituation;

    @JsonProperty("impSaldoActualContravalor")
    private AmountEntity balanceCountervalue;

    @JsonProperty("importeDispAutContravalor")
    private AmountEntity disposibleCountervalue;

    @JsonProperty("importeDispSinAutContravalor")
    private AmountEntity disposibleExclAutCountervalue;

    @JsonProperty("importeLimiteContravalor")
    private AmountEntity amountLimitCountervalue;

    public GeneralInfoEntity getGeneralInfo() {
        return generalInfo;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public AmountEntity getDisposible() {
        return disposible;
    }

    public AmountEntity getDisposibleExclAut() {
        return disposibleExclAut;
    }

    public AmountEntity getAmountLimit() {
        return amountLimit;
    }

    public String getIban() {
        return iban;
    }

    public ContractEntity getOriginalContractId() {
        return originalContractId;
    }

    public CustomerData getCustomerData() {
        return customerData;
    }

    public String getCreditTypeSituation() {
        return creditTypeSituation;
    }

    public AmountEntity getBalanceCountervalue() {
        return balanceCountervalue;
    }

    public AmountEntity getDisposibleCountervalue() {
        return disposibleCountervalue;
    }

    public AmountEntity getDisposibleExclAutCountervalue() {
        return disposibleExclAutCountervalue;
    }

    public AmountEntity getAmountLimitCountervalue() {
        return amountLimitCountervalue;
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount(LoginResponse loginResponse) {
        UserData userData = loginResponse.getUserData();
        HolderName holderName = loginResponse.getHolderName();
        return TransactionalAccount.builder(
                        getTinkAccountType(), getUniqueIdentifier(), balance.getTinkAmount())
                .setAccountNumber(iban)
                .setName(generalInfo.getAlias())
                .setHolderName(holderName)
                .putInTemporaryStorage(
                        SantanderEsConstants.Storage.USER_DATA_XML,
                        SantanderEsXmlUtils.parseJsonToXmlString(userData))
                .putInTemporaryStorage(
                        SantanderEsConstants.Storage.CONTRACT_ID_XML,
                        SantanderEsXmlUtils.parseJsonToXmlString(originalContractId))
                .putInTemporaryStorage(
                        SantanderEsConstants.Storage.BALANCE_XML,
                        SantanderEsXmlUtils.parseJsonToXmlString(getBalance()))
                .build();
    }

    @JsonIgnore
    private String getUniqueIdentifier() {
        return iban.replaceAll(" ", "").toLowerCase();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        if (isCheckingAccount()) {
            return AccountTypes.CHECKING;
        }

        return AccountTypes.OTHER;
    }

    @JsonIgnore
    public boolean isKnownAccountType() {
        // Add more account types as we discover more
        boolean checkingAccount = isCheckingAccount();
        if (!checkingAccount) {

            // Log the whole account entity since they only use numbers and we probably need account
            // name and more
            // in order to figure out what the code stands for.
            log.infoExtraLong(
                    SerializationUtils.serializeToString(this),
                    SantanderEsConstants.Tags.UNKNOWN_ACCOUNT_TYPE);
        }
        return checkingAccount;
    }

    @JsonIgnore
    private boolean isCheckingAccount() {
        String productTypeNumber = generalInfo.getContractId().getProduct();

        // As far as we know today, checking accounts have product number 300 or 301.
        return SantanderEsConstants.AccountTypes.PROD_NR_300.equalsIgnoreCase(productTypeNumber)
                || SantanderEsConstants.AccountTypes.PROD_NR_301.equalsIgnoreCase(
                        productTypeNumber);
    }
}
