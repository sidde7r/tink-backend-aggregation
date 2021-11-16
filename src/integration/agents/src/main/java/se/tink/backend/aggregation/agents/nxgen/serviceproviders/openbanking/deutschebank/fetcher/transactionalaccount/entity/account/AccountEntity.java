package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
@Getter
public class AccountEntity {
    private String resourceId;
    private String product;
    private String iban;
    private String currency;
    private String status;
    private String name;
    private String cashAccountType;
    private String ownerName;
    @Setter private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private AccountLinksWithHrefEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        DeutscheBankConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse(product),
                        TransactionalAccountType.CHECKING)
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(buildAccountName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .addIdentifier(new BbanIdentifier(iban.substring(4)))
                                .build())
                .setApiIdentifier(resourceId)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .addParties(parseOwnerName())
                .build();
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getCreditLimit(balances).ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }

    private String buildAccountName() {
        return Stream.of(name, cashAccountType, product)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(iban);
    }

    private List<Party> parseOwnerName() {
        if (ownerName == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(ownerName.split(";"))
                .map(String::trim)
                .map(owner -> new Party(owner, Role.HOLDER))
                .collect(Collectors.toList());
    }
}
