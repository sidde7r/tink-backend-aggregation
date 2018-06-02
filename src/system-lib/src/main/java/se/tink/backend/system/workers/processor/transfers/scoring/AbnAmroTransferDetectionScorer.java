package se.tink.backend.system.workers.processor.transfers.scoring;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.backend.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AbnAmroTransferDetectionScorer extends DefaultTransferDetectionScorer {

    private final ImmutableMap<String, Account> accountsByIban;

    public AbnAmroTransferDetectionScorer(List<Account> accounts) {

        ImmutableMap.Builder<String, Account> builder = new ImmutableMap.Builder<>();

        for (Account account : accounts) {
            String iban = account.getPayload(AbnAmroUtils.InternalAccountPayloadKeys.IBAN);

            if (Strings.isNullOrEmpty(iban)) {
                continue;
            }

            builder.put(iban, account);
        }

        accountsByIban = builder.build();
    }

    /**
     * Scores transactions based on:
     * 1) (Default)   Score from `DefaultTransferDetectionScorer`
     * 2) (ABN AMRO)  Score 0 or 5. 5 if the transaction matches the iban destination, 0 if it doesn't match.
     *
     * Reason behind the scoring:
     * It should be heavy weight on the iban account since we know for sure that the transaction was made to that
     * account. This scoring means that `iban > max(score date) + max(score description) (5 > 4), which means that
     * a correct iban scores higher than a same day and same description.
     */
    @Override
    public double getScore(Transaction left, Transaction right) {

        double score = super.getScore(left, right);

        String destinationIban = getIbanDestination(left);

        if (Strings.isNullOrEmpty(destinationIban)) {
            return score;
        }

        Account destinationAccount = accountsByIban.get(destinationIban);

        if (destinationAccount == null) {
            return score;
        }

        if (Objects.equals(destinationAccount.getId(), right.getAccountId())) {
            score += 5.0;
        }

        return score;
    }

    private String getIbanDestination(Transaction transaction) {
        String descriptionsLines = transaction.getInternalPayload(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES);

        if (Strings.isNullOrEmpty(descriptionsLines)) {
            return null;
        }

        List<String> lines = SerializationUtils.deserializeFromString(descriptionsLines, TypeReferences.LIST_OF_STRINGS);

        if (lines == null) {
            return null;
        }

        return AbnAmroUtils.getDescriptionParts(lines).get(AbnAmroUtils.DescriptionKeys.IBAN);
    }
}
