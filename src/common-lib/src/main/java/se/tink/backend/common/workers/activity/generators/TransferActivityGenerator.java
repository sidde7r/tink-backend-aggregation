package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;

public class TransferActivityGenerator extends ActivityGenerator {
    public TransferActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(TransferActivityGenerator.class, 30, 40, deepLinkBuilderFactory);

        // Don't show these in "Tink 2.0".
        maxIosVersion = "2.4.9999";
        maxAndroidVersion = "2.4.9999";
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        // Temporarily test without transfer activities internally.
        if (context.getUser().getFlags().contains(FeatureFlags.TINK_EMPLOYEE)) {
            return;
        }

        List<Transaction> transfers = Lists.newArrayList(Iterables.filter(context.getUnusedTransactions(),
                t -> (t.getCategoryType() == CategoryTypes.TRANSFERS)));

        if (transfers.size() == 0) {
            return;
        }

        ImmutableMap<String, Transaction> transfersById = Maps.uniqueIndex(transfers,
                Transaction::getId);

        ImmutableMap<String, Account> accountsById = Maps.uniqueIndex(context.getAccounts(),
                Account::getId);

        Set<String> usedTransferIds = Sets.newHashSet();

        for (Transaction t1 : transfers) {
            if (usedTransferIds.contains(t1.getId())) {
                continue;
            }

            String t2id = t1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN);

            if (usedTransferIds.contains(t2id)) {
                continue;
            }

            Transaction t2 = transfersById.get(t2id);

            Account a1 = accountsById.get(t1.getAccountId());
            Account a2 = accountsById.get(t1.getPayloadValue(TransactionPayloadTypes.TRANSFER_ACCOUNT));

            Account fa = (t1.getAmount() < 0) ? a1 : a2;
            Account ta = (t1.getAmount() < 0) ? a2 : a1;

            if (t2 == null || a1 == null || a2 == null) {
                continue;
            }

            String key = getKey(t1, t2);

            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            t1.getDate(),
                            Activity.Types.TRANSFER,
                            context.getCatalog().getString("Transfer"),
                            Catalog.format(context.getCatalog().getString("You transferred {0} from {1} to {2}"),
                                    I18NUtils.formatCurrency(
                                            Math.abs(t1.getAmount()),
                                            context.getUserCurrency(),
                                            context.getLocale()),
                                    fa.getName(),
                                    ta.getName()),
                            Lists.newArrayList(t1, t2),
                            key,
                            feedActivityIdentifier),
                    Lists.newArrayList(t1, t2));

            usedTransferIds.add(t1.getId());
            usedTransferIds.add(t2.getId());
        }
    }

    private String getKey(Transaction t1, Transaction t2) {
        return Integer.toString(t1.getId().concat(t2.getId()).hashCode());
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
