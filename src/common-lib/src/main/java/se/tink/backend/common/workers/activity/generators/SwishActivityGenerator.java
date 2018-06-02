package se.tink.backend.common.workers.activity.generators;

import java.util.Optional;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TransactionUtils;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

public class SwishActivityGenerator extends ReimbursementActivityGenerator {

    private static final LogUtils log = new LogUtils(SwishActivityGenerator.class);

    public SwishActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(SwishActivityGenerator.class, 10, 50, deepLinkBuilderFactory);

        // TODO: Set minimum versions, matching the clients' support to handle transaction parts.
    }

    @Override
    protected boolean isCandidate(Transaction transaction, Category refundCategory) {
        return super.isCandidate(transaction, refundCategory) && TransactionUtils.isSwish(transaction);
    }

    @Override
    protected String generateTitle(ActivityGeneratorContext context, Transaction transaction) {
        String sender = getSender(context.getCatalog(), transaction);
        return Catalog.format(context.getCatalog().getString("You've received a Swish from {0}"), sender);
    }

    private String getSender(Catalog catalog, Transaction transaction) {
        Optional<String> name = TransactionUtils.getNameFromSwishTransaction(transaction);
        if (name.isPresent()) {
            return name.get();
        }

        Optional<String> phoneNumber = TransactionUtils.getPhoneNumberFromSwishTransaction(transaction);
        if (phoneNumber.isPresent()) {
            return phoneNumber.get();
        }

        log.warn(transaction.getUserId(), transaction.getCredentialsId(),
                String.format("[transactionId:%s] Unable to extract sender from Swish transaction.",
                        transaction.getId()));

        return catalog.getString("someone");
    }

    @Override
    protected String generateMessage(ActivityGeneratorContext context, Transaction transaction) {
        return context.getCatalog().getString("Is it a reimbursement for an expense?");
    }

    // TODO: Add support for notifications

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
