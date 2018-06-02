package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.utils.LogUtils;

public class FraudDataRemoveDetailsProcessor extends FraudDataProcessor {
    private static final LogUtils log = new se.tink.backend.utils.LogUtils(FraudDataRemoveDetailsProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {

        List<FraudDetails> fraudDetails = Lists.newArrayList();

        if (context.getInStoreFraudDetails() != null && context.getInStoreFraudDetails().size() != 0) {
            fraudDetails.addAll(context.getInStoreFraudDetails());
        }

        if (context.getInBatchFraudDetails() != null && context.getInBatchFraudDetails().size() != 0) {
            fraudDetails.addAll(context.getInBatchFraudDetails());
        }

        log.info(
                context.getUser().getId(),
                "In batch and in store fraud details size is "
                        + Iterables.size(fraudDetails));

        // Index all details by type

        ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType = Multimaps.index(fraudDetails,
                FraudDetails::getType);
        
        // Remove non-payments that are older than 3 years (legal reasons). 
        
        Date threeYearsAgo = DateUtils.addYears(new Date(), -3);

        List<FraudDetails> oldFraudDetails = Lists.newArrayList();
        
        for (FraudDetails details : detailsByType.get(FraudDetailsContentType.NON_PAYMENT)) {
            if (details.getDate().before(threeYearsAgo)) {
                oldFraudDetails.add(details);
            }
        }

        log.info(
                context.getUser().getId(),
                "Generated fraud details size for removal list is "
                        + Iterables.size(oldFraudDetails));

        context.getFraudDetailsRemoveList().addAll(oldFraudDetails);
    }

}
