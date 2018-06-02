package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class FraudDataNewDetailsProcessor extends FraudDataProcessor {
    private static final LogUtils log = new se.tink.backend.utils.LogUtils(FraudDataNewDetailsProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {

        List<FraudDetails> newFraudDetails = Lists.newArrayList();

        // Details content list is filtered, go through and generate new details objects.

        log.info(
                context.getUser().getId(),
                "Add in batch fraud details from fraud details content of size "
                        + Iterables.size(context.getFraudDetailsContent()));

        for (FraudDetailsContent detailsContent : context.getFraudDetailsContent()) {
            newFraudDetails.add(createNewFraudDetails(context, detailsContent));
        }

        if (newFraudDetails != null && newFraudDetails.size() != 0) {
            context.getInBatchFraudDetails().addAll(newFraudDetails);
        }
    }

    private FraudDetails createNewFraudDetails(FraudDataProcessorContext context, FraudDetailsContent content) {
        User user = context.getUser();
        FraudDetails details = new FraudDetails();
        details.setCreated(new Date());
        details.setDate(FraudUtils.getDateFromContentType(content));
        details.setUserId(user.getId());
        details.setContent(content);
        details.setType(content.getContentType());
        details.setStatus(FraudUtils.getFraudStatusFromContentType(content.getContentType()));
        return details;
    }
}
