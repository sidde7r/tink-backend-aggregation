package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.Date;
import java.util.List;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.utils.LogUtils;

public class FraudDataBlockedIdentityProcessor extends FraudDataProcessor {

    private static final String BLOCKED_NAME = "person";
    private static final LogUtils log = new LogUtils(FraudDataBlockedIdentityProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {

        // Index all details by type

        ImmutableListMultimap<FraudDetailsContentType, FraudDetailsContent> inBatchDetailsByType = Multimaps.index(context.getFraudDetailsContent(),
                FraudDetailsContent::getContentType);
        
        // If there is a new or old IDENTITY details with name "person", the person is/has been blocked.
        // Remove IDENTITY created before so new aren't deduplicated when the block is lifted/set again.

        List<FraudDetailsContent> identityDetailsContents = inBatchDetailsByType.get(FraudDetailsContentType.IDENTITY);

        if (identityDetailsContents.size() == 0) {
            return;
        }
        
        ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType = Multimaps.index(context.getInStoreFraudDetails(),
                FraudDetails::getType);
        
        List<FraudDetails> identityDetails = detailsByType.get(FraudDetailsContentType.IDENTITY);

        if (identityDetails.size() == 0) {
            return;
        }

        log.info(
                context.getUser().getId(),
                "Identity in store fraud details size is "
                        + Iterables.size(identityDetails));

        List<FraudDetails> fraudDetails = Lists.newArrayList();

        // Find the real IDENTITY and blocked IDENTITY.

        Iterable<FraudDetails> blockedIdentityDetails = Iterables.filter(identityDetails,
                fd -> {
                    FraudIdentityContent identity = (FraudIdentityContent) fd.getContent();
                    return BLOCKED_NAME.equals(identity.getFirstName());
                });

        Iterable<FraudDetails> realIdentityDetails = Iterables.filter(identityDetails,
                fd -> {
                    FraudIdentityContent identity = (FraudIdentityContent) fd.getContent();
                    return !BLOCKED_NAME.equals(identity.getFirstName());
                });

        if (Iterables.size(blockedIdentityDetails) == 0 || Iterables.size(realIdentityDetails) == 0) {
            return;
        }

        // Find newest detail of both and update the oldest of the two.

        FraudDetails realIdentityDetail = FraudUtils.CREATED_ORDERING.max(realIdentityDetails);
        FraudDetails blockedIdentityDetail = FraudUtils.CREATED_ORDERING.max(blockedIdentityDetails);

        if (realIdentityDetail.getCreated().after(blockedIdentityDetail.getCreated())) {
            FraudIdentityContent identity = (FraudIdentityContent) blockedIdentityDetail.getContent();
            identity.setBlocked(new Date());
            blockedIdentityDetail.setContent(identity);
            fraudDetails.add(blockedIdentityDetail);
        } else {
            FraudIdentityContent identity = (FraudIdentityContent) realIdentityDetail.getContent();
            identity.setBlocked(new Date());
            realIdentityDetail.setContent(identity);
            fraudDetails.add(realIdentityDetail);
        }

        log.info(
                context.getUser().getId(),
                "Generated fraud details content size for transactions with fraudulent merchants is "
                        + Iterables.size(fraudDetails));

        context.getFraudDetailsUpdateList().addAll(fraudDetails);
    }
}
