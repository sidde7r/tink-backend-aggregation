package se.tink.backend.aggregation.nxgen.core.to_system;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountParty;
import se.tink.backend.agents.rpc.AccountPartyAddress;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.typeguesser.accountholder.AccountHolderTypeUtil;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.Balance;
import se.tink.backend.aggregation.nxgen.core.account.entity.Address;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

public final class AccountConverter {

    private AccountConverter() {}

    public static se.tink.backend.agents.rpc.Account toSystemAccount(
            User user, Provider provider, Account sourceAccount) {
        se.tink.backend.agents.rpc.Account account = new se.tink.backend.agents.rpc.Account();

        ExactCurrencyAmount exactBalance = sourceAccount.getExactBalance();

        account.setType(sourceAccount.getType());
        account.setName(sourceAccount.getName());
        account.setAccountNumber(sourceAccount.getAccountNumber());
        account.setBalance(exactBalance.getDoubleValue());
        account.setCurrencyCode(exactBalance.getCurrencyCode());
        account.setExactBalance(exactBalance);
        account.setIdentifiers(sourceAccount.getIdentifiers());
        account.setBankId(sourceAccount.getUniqueIdentifier());
        account.setHolderName(sourceAccount.getFirstHolder().orElse(null));
        account.setFlags(sourceAccount.getAccountFlags());
        account.setPayload(createAccountPayload(user, sourceAccount));
        account.setAvailableCredit(
                Optional.ofNullable(sourceAccount.getExactAvailableCredit())
                        .map(ExactCurrencyAmount::getDoubleValue)
                        .orElse(0.0));
        account.setExactAvailableCredit(sourceAccount.getExactAvailableCredit());
        account.setAvailableBalance(sourceAccount.getExactAvailableBalance());
        account.setCreditLimit(sourceAccount.getExactCreditLimit());
        account.setCapabilities(sourceAccount.getCapabilities());
        account.setSourceInfo(sourceAccount.getSourceInfo());

        AccountHolder accountHolder = new AccountHolder();
        accountHolder.setType(
                Optional.ofNullable(sourceAccount.getHolderType())
                        .orElse(inferHolderType(provider))
                        .toSystemType());
        accountHolder.setIdentities(
                sourceAccount.getParties().stream()
                        .map(AccountConverter::toSystemParty)
                        .collect(Collectors.toList()));
        account.setAccountHolder(accountHolder);

        account.setBalances(
                sourceAccount.getBalances() != null
                        ? sourceAccount.getBalances().stream()
                                .map(Balance::toSystemBalance)
                                .collect(Collectors.toList())
                        : ImmutableList.of());
        return account;
    }

    private static String createAccountPayload(User user, Account sourceAccount) {
        Map<String, String> payload = sourceAccount.getPayload();
        if (FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags())) {
            payload.put("currency", sourceAccount.getExactBalance().getCurrencyCode());
        }

        if (payload.isEmpty()) {
            return null;
        }

        return SerializationUtils.serializeToString(payload);
    }

    private static AccountHolderType inferHolderType(Provider provider) {
        String accountHolderTypeAsString = AccountHolderTypeUtil.inferHolderType(provider).name();
        return AccountHolderType.valueOf(accountHolderTypeAsString);
    }

    private static AccountParty toSystemParty(Party party) {
        AccountParty systemParty = new AccountParty();
        systemParty.setName(party.getName());
        systemParty.setRole(toSystemPartyRole(party.getRole()));
        systemParty.setAddresses(toSystemPartyAddress(party.getAddresses()));
        return systemParty;
    }

    private static HolderRole toSystemPartyRole(Party.Role role) {
        if (role == Party.Role.UNKNOWN) {
            return null;
        }
        return HolderRole.valueOf(role.name());
    }

    private static List<AccountPartyAddress> toSystemPartyAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;

        return addresses.stream()
                .map(
                        address ->
                                new AccountPartyAddress(
                                        address.getAddressType(),
                                        address.getStreet(),
                                        address.getPostalCode(),
                                        address.getCity(),
                                        address.getCountry()))
                .collect(Collectors.toList());
    }
}
