package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.BalanceType;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class AccountTrackingSerializer extends TrackingMapSerializer {

    private static final Map<String, Function<Account, String>> FIELD_VALUE_EXTRACTOR =
            new HashMap<>();

    static {
        FIELD_VALUE_EXTRACTOR.put("accountNumber", Account::getAccountNumber);
        FIELD_VALUE_EXTRACTOR.put("bankId", Account::getBankId);
        FIELD_VALUE_EXTRACTOR.put("name", Account::getName);
        FIELD_VALUE_EXTRACTOR.put("holderName", Account::getHolderName);
        FIELD_VALUE_EXTRACTOR.put("balance", account -> account.getNullableBalance().toString());
        FIELD_VALUE_EXTRACTOR.put(
                "availableCredit", account -> account.getNullableAvailableCredit().toString());
        FIELD_VALUE_EXTRACTOR.put("type", account -> account.getType().name());

        for (AccountIdentifierType type : AccountIdentifierType.values()) {
            FIELD_VALUE_EXTRACTOR.put(
                    "identifiers." + type.toString(),
                    account ->
                            account.getIdentifiers().stream()
                                    .filter(identifier -> type.equals(identifier.getType()))
                                    .findFirst()
                                    .get()
                                    .getIdentifier());
        }

        for (BalanceType type : BalanceType.values()) {
            FIELD_VALUE_EXTRACTOR.put(
                    "balances." + type.toString(),
                    account ->
                            String.valueOf(
                                    account.getBalances().stream()
                                            .filter(b -> b.getType() == type)
                                            .findFirst()
                                            .get()
                                            .getAmount()
                                            .getDoubleValue()));
        }

        FIELD_VALUE_EXTRACTOR.put(
                "exactBalance.value",
                account -> String.valueOf(account.getExactBalance().getDoubleValue()));
        FIELD_VALUE_EXTRACTOR.put(
                "exactBalance.currency", account -> account.getExactBalance().getCurrencyCode());

        FIELD_VALUE_EXTRACTOR.put(
                "availableBalance.value",
                account -> String.valueOf(account.getAvailableBalance().getDoubleValue()));
        FIELD_VALUE_EXTRACTOR.put(
                "availableBalance.currency",
                account -> account.getAvailableBalance().getCurrencyCode());
        FIELD_VALUE_EXTRACTOR.put(
                "accountHolder", account -> account.getAccountHolder().toString());
    }

    private static final String ACCOUNT_ENTITY_NAME = "Account";
    private final Account account;
    private final Integer numberOfTransactions;

    public AccountTrackingSerializer(Account account, Integer numberOfTransactions) {
        super(String.format(ACCOUNT_ENTITY_NAME + "<%s>", account.getType()));
        this.account = account;
        this.numberOfTransactions = numberOfTransactions;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {

        addFields(listBuilder);
        listBuilder.putListed("numberOfTransactions", Integer.toString(numberOfTransactions));

        // Accounts without flags are currently valid, so we do not track this as null if empty.
        account.getFlags().forEach(value -> listBuilder.putListed("flags", value));

        if (Objects.nonNull(account.getAccountHolder())) {
            String key = "accountHolder<" + account.getAccountHolder().getType().name() + ">";
            listBuilder.putRedacted(key + ".accountId", account.getAccountHolder().getAccountId());
            listBuilder.putRedacted(
                    key + ".identities", account.getAccountHolder().getIdentities().toString());
            for (HolderIdentity holderIdentity : account.getAccountHolder().getIdentities()) {
                String holderIdentityKey =
                        key + ".identities<" + holderIdentity.getRole().name() + ">";
                listBuilder.putRedacted(holderIdentityKey + ".name", holderIdentity.getName());
            }
        }

        return listBuilder.build();
    }

    private void addFields(TrackingList.Builder listBuilder) {
        for (Map.Entry<String, Function<Account, String>> entry :
                FIELD_VALUE_EXTRACTOR.entrySet()) {
            String fieldName = entry.getKey();
            try {
                String fieldValue = entry.getValue().apply(account);
                if (Objects.nonNull(fieldValue) && !fieldValue.isEmpty()) {
                    listBuilder.putRedacted(fieldName, fieldValue);
                } else {
                    listBuilder.putNull(fieldName);
                }
            } catch (NullPointerException | NoSuchElementException e) {
                listBuilder.putNull(fieldName);
            }
        }
    }
}
