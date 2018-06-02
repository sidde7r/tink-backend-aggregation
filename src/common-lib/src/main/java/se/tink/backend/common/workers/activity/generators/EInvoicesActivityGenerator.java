package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class EInvoicesActivityGenerator extends ActivityGenerator {

    public EInvoicesActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(EInvoicesActivityGenerator.class, 70, deepLinkBuilderFactory);

        minAndroidVersion = "2.5.3";
        minIosVersion = "2.5.0";
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        TransferRepository transferRepository = context.getServiceContext().getRepository(TransferRepository.class);

        Iterable<Transfer> eInvoices = Iterables.filter(transferRepository.findAllByUserId(context.getUser().getId()),
                Predicates.TRANSFER_EINVOICES);

        if (Iterables.size(eInvoices) == 0) {
            return;
        }

        // Sort eInvoices by due date ( descending )
        List<Transfer> orderedEInvoices = Ordering.from(Comparator.comparing(Transfer::getDueDate,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .sortedCopy(eInvoices);

        // Use the dueDate of the latest e-invoice as activity date
        Date activityDate = orderedEInvoices.get(0).getDueDate();

        String feedActivityIdentifier = generateFeedActivityIdentifier(orderedEInvoices);
        String activityKey = String.format("EInvoices.%s", feedActivityIdentifier);

        String title = context.getCatalog().getPluralString(
                "E-invoice to approve",
                "E-invoices to approve",
                Iterables.size(eInvoices));

        String message = "";

        EInvoicesActivityData content = new EInvoicesActivityData();
        content.setTransfers(Lists.newArrayList(eInvoices));

        context.addActivity(createActivity(
                context.getUser().getId(),
                activityDate,
                Activity.Types.EINVOICES,
                title,
                message,
                content,
                activityKey,
                feedActivityIdentifier));
    }

    private String generateFeedActivityIdentifier(Iterable<Transfer> eInvoices) {
        StringBuilder builder = new StringBuilder();

        for (Transfer eInvoice : eInvoices) {
            builder.append(eInvoice.getHashIgnoreSource());
        }

        return StringUtils.hashAsStringSHA1(builder.toString());
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
