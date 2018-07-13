package se.tink.backend.aggregation.agents.abnamro.ics.mappers;

import com.google.common.base.Strings;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.abnamro.client.model.creditcards.TransactionContainerEntity;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;

public class TransactionMapper {

    public static Transaction toTransaction(TransactionContainerEntity input) {
        Transaction transaction = new Transaction();
        transaction.setDate(input.getCreditCardTransaction().getDate());
        transaction.setDescription(Strings.nullToEmpty(input.getCreditCardTransaction().getDescription()).trim());
        transaction.setAmount(input.getAmount());
        transaction.setPending(input.isPending());

        String merchantDescription = input.getCreditCardTransaction().getMerchantDescription();

        if (!Strings.isNullOrEmpty(merchantDescription)) {
            transaction.setInternalPayload(AbnAmroUtils.InternalPayloadKeys.MERCHANT_DESCRIPTION,
                    merchantDescription.trim().toUpperCase());
        }

        return transaction;
    }
}
