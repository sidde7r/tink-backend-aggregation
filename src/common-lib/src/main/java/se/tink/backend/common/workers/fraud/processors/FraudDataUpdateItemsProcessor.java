package se.tink.backend.common.workers.fraud.processors;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.List;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.utils.LogUtils;

public class FraudDataUpdateItemsProcessor extends FraudDataProcessor {

    private static final LogUtils log = new se.tink.backend.utils.LogUtils(FraudDataEmptyStatesProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {

        List<FraudDetails> details = Lists.newArrayList();
        details.addAll(context.getInStoreFraudDetails());
        details.addAll(context.getInBatchFraudDetails());

        // Index details on itemType.
        
        ImmutableListMultimap<FraudTypes, FraudDetails> detailsByItemType = Multimaps.index(details,
                fd -> fd.getContent().itemType());

        log.info(
                context.getUser().getId(),
                "Fraud details item to update size is "
                        + Iterables.size(details));

        // Set new status on the items that have new details.

        for (FraudItem item : context.getInStoreFraudItems()) {
            ImmutableList<FraudDetails> detailsForType = detailsByItemType.get(item.getType());

            int unhandledDetailsCount = 0;
            FraudStatus status = null;

            for (FraudDetails detail : detailsForType) {
                if (detail.getStatus() == FraudStatus.WARNING || detail.getStatus() == FraudStatus.CRITICAL) {
                    unhandledDetailsCount++;
                    status = detail.getStatus();
                }

                detail.setFraudItemId(item.getId());
            }

            if (unhandledDetailsCount == 0) {
                item.setStatus(FraudStatus.OK);
            } else {
                item.setStatus(status);
            }

            item.setUnhandledDetailsCount(unhandledDetailsCount);

            // Update sources on TRANSACTION item with credentials' provider display name.

            if (item.getType() == FraudTypes.TRANSACTION) {
                List<String> sources = Lists.newArrayList();
                sources.addAll(FraudUtils.getSourcesFromItemType(item.getType()));

                for (Provider p : context.getProviders()) {
                    sources.add(p.getDisplayName());
                }

                item.setSources(sources);
            }

            // Update unseen count on item.

            for (FraudDetails detail : context.getInBatchFraudDetails()) {
                if (detail.getStatus() != FraudStatus.EMPTY && Objects.equal(detail.getFraudItemId(), item.getId())) {
                    item.setUnseenDetailsCount(item.getUnseenDetailsCount() + 1);
                }
            }
        }
    }
}
