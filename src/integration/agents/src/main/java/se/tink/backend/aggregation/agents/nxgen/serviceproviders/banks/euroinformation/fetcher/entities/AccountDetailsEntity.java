package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
@XmlRootElement(name = "compte")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountDetailsEntity {
    @XmlElement(name = "account_type")
    private String accountType;

    private String iban;

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "devise")
    private String currency;

    @XmlElement(name = "intc")
    private String accountNameAndNumber;

    @XmlElement(name = "int")
    private String accountName;

    @XmlElement(name = "tit")
    private String title;

    private String codprod;

    @XmlElement(name = "category_code")
    private String categoryCode;

    @XmlElement(name = "category_name")
    private String categoryName;

    @XmlElement(name = "solde")
    private String amountToParse;

    @XmlElement(name = "transactions_to_come")
    private String transactionsToCome;

    @XmlElement(name = "webid")
    private String webId;

    private String contract;
    private String refprd;

    @XmlElement(name = "refctr_exi_val")
    private String refctrExiVal;

    @XmlElement(name = "refctr_inn_val")
    private String refctrInnVal;

    @XmlElement(name = "agreed_overdraft")
    private String agreedOverdraft;

    @XmlElement(name = "amount_available")
    private String amountAvailable;

    private String appcpt;

    @XmlElement(name = "isholder")
    private String isholder;

    private String checkingaccount;
    private String characteristics;
    private String simulation;

    public String getContract() {
        return contract;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountName() {
        return accountName;
    }

    public Party isHolder(String clientName) {
        return "1".equals(isholder)
                ? new Party(clientName, Party.Role.HOLDER)
                : new Party(clientName, Party.Role.OTHER);
    }

    @JsonIgnore
    private String decideUniqueIdentifier() {
        if (!Strings.isNullOrEmpty(iban)) {
            return iban;
        }
        if (!Strings.isNullOrEmpty(accountNumber)) {
            return accountNumber;
        }
        return parseAccountNumberFromName();
    }

    @JsonIgnore
    private String parseAccountNumberFromName() {
        return accountNameAndNumber.split(accountName)[0].toLowerCase();
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(Party party) {
        return EuroInformationConstants.ACCOUNT_TYPE_MAPPER
                .translate(accountType)
                .flatMap(
                        accType ->
                                (party == null)
                                        ? buildAccountWithoutHolder(accType).build()
                                        : buildAccountWithoutHolder(accType)
                                                .addParties(party)
                                                .build());
    }

    private TransactionalBuildStep buildAccountWithoutHolder(TransactionalAccountType accType) {
        return TransactionalAccount.nxBuilder()
                .withType(accType)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(EuroInformationUtils.parseAmount(amountToParse, currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(decideUniqueIdentifier())
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .putInTemporaryStorage(EuroInformationConstants.Tags.WEB_ID, webId);
    }
}
