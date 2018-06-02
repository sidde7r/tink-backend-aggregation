package se.tink.backend.common.workers.fraud.processors;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.List;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudCreditorContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.utils.LogUtils;

/**
 * Create empty states details for all types where there is no data. Remove empty states details if there now is data.
 */
public class FraudDataEmptyStatesProcessor extends FraudDataProcessor {

    private static final ImmutableList<FraudDetailsContentType> DISABLED_TYPES = ImmutableList.of();

    private static final LogUtils log = new se.tink.backend.utils.LogUtils(FraudDataEmptyStatesProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {
        List<FraudDetails> fraudDetails = Lists.newArrayList();

        if (context.getInStoreFraudDetails() != null && context.getInStoreFraudDetails().size() != 0) {
            fraudDetails.addAll(context.getInStoreFraudDetails());
        }

        if (context.getInBatchFraudDetails() != null && context.getInBatchFraudDetails().size() != 0) {
            fraudDetails.addAll(context.getInBatchFraudDetails());
        }

        // Index all details by type

        ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType = Multimaps.index(fraudDetails,
                FraudDetails::getType);

        // If there is just one CREDITS details and has amount = 0, set it to EMPTY.

        List<FraudDetails> creditsDetails = detailsByType.get(FraudDetailsContentType.CREDITS);

        if (creditsDetails.size() == 1) {
            FraudDetails creditDetails = creditsDetails.get(0);
            FraudCreditorContent creditContent = (FraudCreditorContent) creditDetails.getContent();
            if (creditContent.getAmount() == 0) {
                creditDetails.setStatus(FraudStatus.EMPTY);
            }
        }
        
        // Loop all types, add empty state details if no real exists, remove old empty state details if real exists.

        List<FraudDetails> emptyFraudDetails = Lists.newArrayList();
        List<FraudDetails> fraudDetailsToRemove = Lists.newArrayList();

        typeLoop: for (FraudDetailsContentType type : FraudDetailsContentType.values()) {

            if (DISABLED_TYPES.contains(type)) {
                continue;
            }

            List<FraudDetails> detailsForType = detailsByType.get(type);

            if (detailsForType.size() == 0) {
                emptyFraudDetails.add(FraudUtils.getEmptyStateContentFromType(context.getUser(), type));
                continue;
            }

            FraudDetails nonEmpty = null;
            FraudDetails empty = null;

            for (FraudDetails details : detailsForType) {
                if (details.getStatus() != FraudStatus.EMPTY) {
                    nonEmpty = details;
                }

                if (details.getStatus() == FraudStatus.EMPTY) {
                    empty = details;
                }

                if (nonEmpty != null && empty != null) {
                    fraudDetailsToRemove.add(empty);
                    continue typeLoop;
                }
            }
        }

        log.info(
                context.getUser().getId(),
                "Generate new empty fraud details of size "
                        + Iterables.size(emptyFraudDetails));

        log.info(
                context.getUser().getId(),
                "Empty fraud details to remove size is "
                        + Iterables.size(fraudDetailsToRemove));

        context.getInBatchFraudDetails().addAll(emptyFraudDetails);
        context.getFraudDetailsRemoveList().addAll(fraudDetailsToRemove);
    }
}
