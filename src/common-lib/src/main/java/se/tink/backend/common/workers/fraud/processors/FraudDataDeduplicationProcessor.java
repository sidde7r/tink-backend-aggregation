package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.utils.LogUtils;

public class FraudDataDeduplicationProcessor extends FraudDataProcessor {
    private static final LogUtils log = new se.tink.backend.utils.LogUtils(FraudDataDeduplicationProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {

        // Index details content on content id.

        ImmutableListMultimap<String, FraudDetailsContent> detailsContentById = Multimaps.index(
                context.getFraudDetailsContent(), FraudDetailsContent::getContentId);

        log.info(
                context.getUser().getId(),
                "Size of fraud details content is "
                        + Iterables.size(context.getFraudDetailsContent()));

        // Index details from db on content id.

        Set<String> inStoreDetailIds = context.getInStoreFraudDetails().stream()
                .map(fd -> fd.getContent().getContentId())
                .collect(Collectors.toSet());

        // Only add non duplicates.

        List<FraudDetailsContent> detailsContentResult = Lists.newArrayList();

        // Loop details contents and add only non duplicates.

        for (FraudDetailsContent detailsContent : detailsContentById.values()) {
            if (!isDuplicate(context.getInStoreFraudDetails(), inStoreDetailIds, detailsContent)) {
                detailsContentResult.add(detailsContent);
                inStoreDetailIds.add(detailsContent.getContentId());
            }
        }

        // Reset context with filtered details content list.

        log.info(
                context.getUser().getId(),
                "Fraud details content size after deduplication is "
                        + Iterables.size(detailsContentResult));

        context.setFraudDetailsContent(detailsContentResult);
    }

    private boolean isDuplicate(List<FraudDetails> inStoreFraudDetails, Set<String> inStoreDetailsContentIds,
            FraudDetailsContent detailsContent) {
        if (Objects.equals(detailsContent.getContentType(), FraudDetailsContentType.ADDRESS)) {
            // When checking registered address, we should only check the most recent one.
            // Otherwise moving `Address A -> Address B -> Address A` would deduplicate the last entry.
            return isMostRecentAddress(inStoreFraudDetails, detailsContent);
        } else {
            return inStoreDetailsContentIds.contains(detailsContent.getContentId());
        }
    }

    private boolean isMostRecentAddress(List<FraudDetails> inStoreFraudDetails, FraudDetailsContent detailsContent) {
        Optional<FraudDetails> mostRecentFraudDetails = FraudUtils.getMostRecentFraudDetailsByType(inStoreFraudDetails, FraudDetailsContentType.ADDRESS);
        if (!mostRecentFraudDetails.isPresent()) {
            return false;
        } else if (Objects.equals(mostRecentFraudDetails.get().getContent().getContentId(), detailsContent.getContentId())) {
            return true;
        } else {
            return false;
        }
    }
}
