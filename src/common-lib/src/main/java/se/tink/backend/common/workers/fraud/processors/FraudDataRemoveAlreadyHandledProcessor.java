package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.backend.core.FraudTypes;
import se.tink.backend.utils.LogUtils;

/**
 * Removed already handled fraud transaction details with same description.
 */
public class FraudDataRemoveAlreadyHandledProcessor extends FraudDataProcessor {

    private static final LogUtils log = new se.tink.backend.utils.LogUtils(
            FraudDataRemoveAlreadyHandledProcessor.class);

    @Override
    public void process(FraudDataProcessorContext context) {
        Iterable<FraudDetailsContent> inBatchFraudTransactionDetails = Iterables.filter(
                context.getFraudDetailsContent(),
                input -> input.itemType() == FraudTypes.TRANSACTION);

        Iterable<FraudDetails> inStoreFraudTransactionDetails = Iterables.filter(context.getInStoreFraudDetails(),
                input -> input.getContent().itemType() == FraudTypes.TRANSACTION);

        if (Iterables.size(inBatchFraudTransactionDetails) == 0
                || Iterables.size(inStoreFraudTransactionDetails) == 0) {
            return;
        }

        log.info(
                context.getUser().getId(),
                "In batch content details before removing already handled is "
                        + Iterables.size(inBatchFraudTransactionDetails));

        // Create a set of descriptions from transactions that are already handled by content type.

        Map<FraudDetailsContentType, Set<String>> handledTransactionDescriptionsByContentType = Maps.newHashMap();
        List<FraudDetailsContent> filteredFraudDetailsContent = Lists.newArrayList();

        for (FraudDetails details : inStoreFraudTransactionDetails) {

            if (details.getStatus() != FraudStatus.OK) {
                continue;
            }

            Set<String> handledTransactionDescriptions = handledTransactionDescriptionsByContentType.get(details
                    .getType());

            if (handledTransactionDescriptions == null) {
                handledTransactionDescriptions = Sets.newHashSet();
                handledTransactionDescriptionsByContentType.put(details.getType(), handledTransactionDescriptions);
            }

            FraudTransactionContent transactionContent = (FraudTransactionContent) details.getContent();

            for (FraudTransactionEntity t : transactionContent.getTransactions()) {
                handledTransactionDescriptions.add(t.getDescription());
            }
        }

        // Check all in batch detailsContent against the handled description set.

        for (FraudDetailsContent detailsContent : inBatchFraudTransactionDetails) {
            FraudTransactionContent transactionContent = (FraudTransactionContent) detailsContent;

            Set<String> handledTransactionDescriptions = handledTransactionDescriptionsByContentType.get(detailsContent
                    .getContentType());
            
            if (handledTransactionDescriptions == null) {
                filteredFraudDetailsContent.add(detailsContent);
                continue;
            }

            boolean allDescriptionAlreadyHandled = true;

            for (FraudTransactionEntity transaction : transactionContent.getTransactions()) {
                if (!handledTransactionDescriptions.contains(transaction.getDescription())) {
                    allDescriptionAlreadyHandled = false;
                    break;
                }
            }

            // If allDescriptionAlreadyHandled == true than remove this contentDetails from the
            // list since the user has already seen and marked the same transaction OK.

            if (!allDescriptionAlreadyHandled || transactionContent.getTransactions().isEmpty()) {
                filteredFraudDetailsContent.add(detailsContent);
            }
        }

        log.info(
                context.getUser().getId(),
                "In batch content details after removing already handled is "
                        + Iterables.size(filteredFraudDetailsContent));

        List<FraudDetailsContent> fraudDetailsContents = context.getFraudDetailsContent();
        fraudDetailsContents.removeAll(Lists.newArrayList(inBatchFraudTransactionDetails));
        fraudDetailsContents.addAll(filteredFraudDetailsContent);
        context.setFraudDetailsContent(fraudDetailsContents);
    }
}
