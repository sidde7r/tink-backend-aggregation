package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String accountType;
    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String id;
    private String maskedPan;
    private String msisdn;
    private String name;
    private String resourceId;
    private String product;
    private List<BalanceEntity> balances;
    private String ownerName;

    public LinksEntity getLinks() {
        return links;
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        if (iban == null) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER, getAccountType())
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(bic, iban))
                                .addIdentifier(new BbanIdentifier(getBban()))
                                .build())
                .addParties(new Party(ownerName, Role.HOLDER))
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getAccountNumber())
                .build();
    }

    public CreditCardAccount toTinkCreditAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan)
                                .withBalance(balances.get(0).toTinkAmount())
                                .withAvailableCredit(balances.get(0).toTinkAmount())
                                .withCardAlias(name)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(new MaskedPanIdentifier(maskedPan))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .setBankIdentifier(getAccountNumber())
                .build();
    }

    public boolean isCreditCardAccount() {
        return !Strings.isNullOrEmpty(maskedPan);
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public String getAccountType() {
        return ObjectUtils.firstNonNull(accountType, cashAccountType, product);
    }

    public void setBalance(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getCreditLimit(balances).ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }

    private String getAccountNumber() {
        if (!Strings.isNullOrEmpty(iban)) {
            return iban;
        } else if (!Strings.isNullOrEmpty(maskedPan)) {
            return maskedPan;
        }
        return bic;
    }

    private String getBban() {
        if (bban != null) {
            return bban;
        }
        if (iban != null) {
            return iban.substring(4);
        }
        return null;
    }

    private String getAccountName() {
        return ObjectUtils.firstNonNull(name, product, getAccountType());
    }
}
