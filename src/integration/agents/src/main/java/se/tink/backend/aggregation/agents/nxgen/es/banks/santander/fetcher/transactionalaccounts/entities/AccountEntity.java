package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.CustomerData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

@JsonObject
@XmlRootElement
public class AccountEntity {
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
    public Optional<TransactionalAccount> toTinkAccount(LoginResponse loginResponse) {
        UserData userData = loginResponse.getUserData();
        HolderName holderName = loginResponse.getHolderName();

        AccountIdentifier ibanIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, iban.replaceAll("\\s+", ""));
        DisplayAccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(getTinkAccountType()).get())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(balance.getTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(ibanIdentifier.getIdentifier(formatter))
                                .withAccountName(generalInfo.getAlias())
                                .addIdentifier(ibanIdentifier)
                                .build())
                .addParties(getParties(holderName, customerData))
                .setBankIdentifier(iban)
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
    private List<Party> getParties(HolderName holderName, CustomerData customerData) {
        if (!isPersonTypeHolder(customerData)) {
            // Other person types does not provide holder name
            return Collections.emptyList();
        }

        return Collections.singletonList(new Party(holderName.toString(), Party.Role.HOLDER));
    }

    @JsonIgnore
    private boolean isPersonTypeHolder(CustomerData customerData) {
        return SantanderEsConstants.PersonType.HOLDER.equals(customerData.getPersonType());
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
        return isCheckingAccount();
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
