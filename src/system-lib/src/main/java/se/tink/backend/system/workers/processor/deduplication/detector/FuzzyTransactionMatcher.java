package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

class FuzzyTransactionMatcher {
    private static final LogUtils log = new LogUtils(FuzzyTransactionMatcher.class);
    private static final double MAX = 1;
    private static final double MIN = 0;

    private static ImmutableMap<String, Pattern> pendingDescriptionPatterns = ImmutableMap.<String, Pattern>builder()
            .put("swedbank och sparbankerna", Pattern.compile("^skyddat belopp$|^övf via internet$",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
            .put("handelsbanken", Pattern.compile("^prel(\\.(\\s.+)?)?$", Pattern.CASE_INSENSITIVE))
            .put("länsförsäkringar bank", Pattern.compile("^prel(\\s.+)?$", Pattern.CASE_INSENSITIVE))
            .build();

    private final Provider provider;
    private final Transaction existingTransaction;
    private final Transaction incomingTransaction;

    private double amountScore;
    private double dateScore;
    private double descriptionScore;

    private final boolean possibleMatchesAreIncoming;

    private FuzzyTransactionMatcher(Transaction transaction, Transaction possibleMatch, Provider provider,
            boolean possibleMatchAreIncoming) {
        this.provider = provider;
        this.possibleMatchesAreIncoming = possibleMatchAreIncoming;

        if (possibleMatchAreIncoming) {
            this.existingTransaction = transaction;
            this.incomingTransaction = possibleMatch;
        } else {
            this.existingTransaction = possibleMatch;
            this.incomingTransaction = transaction;
        }
    }

    /**
     * This method is used to compare two transactions (existing, incoming) or (incoming, existing)
     * In order for this class to know which transaction is supposed to be settled e.t.c.
     * we need to know which transaction is existing vs incoming.
     *
     * @param transaction Could be either an existing or an incoming transaction
     * @param possibleMatch Could be either an existing or an incoming transaction (opposite of @param transaction)
     * @param provider The Bank Provider from where the transactions were fetched
     * @param possibleMatchAreIncoming Specifies whether or not the possibleMatch is existing or incoming
     * @return
     */
    static Result compare(Transaction transaction, Transaction possibleMatch, Provider provider,
            boolean possibleMatchAreIncoming) {
        FuzzyTransactionMatcher matcher = new FuzzyTransactionMatcher(transaction, possibleMatch, provider,
                possibleMatchAreIncoming);

        if (matcher.hasExternalIds() || !matcher.belongToSameAccount()) {
            return matcher.result();
        }

        matcher.compareDate();
        matcher.compareDescription();
        matcher.compareAmount();

        return matcher.result();
    }

    private void compareDescription() {
        // Null description should be interpreted as empty description
        this.descriptionScore = StringUtils.getJaroWinklerDistance(
                Strings.nullToEmpty(existingTransaction.getOriginalDescription()),
                Strings.nullToEmpty(incomingTransaction.getOriginalDescription()));
    }

    private void compareAmount() {
        double maxDiffQuotient = 0.40;
        double existingAmount = existingTransaction.getOriginalAmount();
        double incomingAmount = incomingTransaction.getOriginalAmount();

        if (isSignDifferent(existingAmount, incomingAmount)) {
            this.amountScore = MIN;
            return;
        }

        double diff = Math.abs(existingAmount - incomingAmount);

        if (diff == 0) {
            this.amountScore = MAX;
            return;
        }

        double diffQuotient = diff / existingAmount;
        if (diffQuotient > maxDiffQuotient) {
            this.amountScore = MIN;
            return;
        }

        // Don't change. This is used to scale ( weight * diffQuotient ) to a number between 0 and 0.5.
        double weight = 0.5 / maxDiffQuotient;

        this.amountScore = Math.cos(weight * diffQuotient * Math.PI);
    }

    private void compareDate() {
        int maxDaysBetween = 20;

        int daysBetween = DateUtils.daysBetween(
                existingTransaction.getOriginalDate(),
                incomingTransaction.getOriginalDate());

        if (daysBetween == 0) {
            this.dateScore = MAX;
            return;
        }

        if (daysBetween > maxDaysBetween || daysBetween < 0) {
            this.dateScore = MIN;
            return;
        }

        // Don't change. This is used to scale ( weight * daysBetween ) to a number between 0 and 0.5.
        double weight = 0.5 / maxDaysBetween;

        this.dateScore = Math.cos(weight * daysBetween * Math.PI);
    }

    private Result result() {
        return new Result(getTransaction(), getPossibleMatch(), descriptionScore, dateScore, amountScore,
                calculateScore());
    }

    private double calculateScore() {
        if (incomingTransaction == null || !belongToSameAccount()) {
            return MIN;
        }
        if (hasExternalIds()) {
            return Transactions.matchingExternalIds(existingTransaction, incomingTransaction) ? MAX : MIN;
        }

        if (!existingTransaction.isPending()) {
            if (incomingTransaction.isPending() || descriptionScore < 1 || dateScore < 1 || amountScore < 1) {
                return MIN;
            }

            return MAX;
        }

        double amountScore = this.amountScore;
        double dateScore = this.dateScore;
        double descriptionScore = this.descriptionScore;
        int normalizationFactor = 3;

        if (incomingTransaction.isPending()) {
            amountScore = amountScore < 1 ? MIN : amountScore;
            descriptionScore = descriptionScore < 1 ? MIN : descriptionScore;
        } else {
            amountScore = amountScore * 3;

            if (isPredefinedPendingDescription(provider, existingTransaction.getOriginalDescription())) {
                dateScore = dateScore * 2;
                descriptionScore = MAX;
            } else {
                descriptionScore = descriptionScore * 2;
            }

            normalizationFactor = 6;
        }

        // If any of the scores are zero we should say that they are not matching transactions
        if (Objects.equals(amountScore, MIN)
                || Objects.equals(dateScore, MIN)
                || Objects.equals(descriptionScore, MIN)) {
            return MIN;
        }

        double totalScore = amountScore + dateScore + descriptionScore;

        return totalScore / normalizationFactor;
    }

    private Transaction getTransaction() {
        return possibleMatchesAreIncoming ? existingTransaction : incomingTransaction;
    }

    private Transaction getPossibleMatch() {
        return possibleMatchesAreIncoming ? incomingTransaction : existingTransaction;
    }

    private boolean hasExternalIds() {
        return Transactions.hasExternalIds(existingTransaction, incomingTransaction);
    }

    private boolean belongToSameAccount() {
        return Objects.equals(existingTransaction.getAccountId(), incomingTransaction.getAccountId());
    }

    @VisibleForTesting
    static boolean isPredefinedPendingDescription(Provider provider, String description) {
        String providerName = Optional.ofNullable(provider.getGroupDisplayName()).orElse(provider.getDisplayName());

        Pattern pendingDescriptionPattern = pendingDescriptionPatterns.get(providerName.toLowerCase());

        return pendingDescriptionPattern != null && pendingDescriptionPattern.matcher(description).matches();
    }

    @VisibleForTesting
    static boolean isSignDifferent(double existingAmount, double incomingAmount) {
        if (existingAmount == 0d || incomingAmount == 0d) {
            log.warn(String.format("Transaction amount is zero {\"existingAmount\": %s, \"incomingAmount\": %s}",
                    existingAmount, incomingAmount));
            return false;
        }

        return existingAmount > 0 ^ incomingAmount > 0;
    }

    private static class Transactions {
        private static boolean hasExternalIds(Transaction t1, Transaction t2) {
            return !Strings.isNullOrEmpty(getExternalId(t1)) && !Strings.isNullOrEmpty(getExternalId(t2));
        }

        private static boolean matchingExternalIds(Transaction t1, Transaction t2) {
            return Objects.equals(getExternalId(t1), getExternalId(t2));
        }

        private static String getExternalId(Transaction transaction) {
            return transaction.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID);
        }
    }

    static class Result {
        private final Transaction transaction;
        private final Optional<Transaction> possibleMatch;

        private final double descriptionScore;
        private final double dateScore;
        private final double amountScore;
        private final double score;

        Result(Transaction transaction) {
            this(transaction, null, 0, 0, 0, 0);
        }

        private Result(Transaction transaction, Transaction possibleMatch, double descriptionScore, double dateScore,
                double amountScore, double score) {
            this.transaction = transaction;
            this.possibleMatch = Optional.ofNullable(possibleMatch);
            this.descriptionScore = descriptionScore;

            this.dateScore = dateScore;
            this.amountScore = amountScore;
            this.score = score;
        }

        Transaction getTransaction() {
            return transaction;
        }

        Optional<Transaction> getPossibleMatch() {
            return possibleMatch;
        }

        @VisibleForTesting
        double getDescriptionScore() {
            return descriptionScore;
        }

        @VisibleForTesting
        double getDateScore() {
            return dateScore;
        }

        @VisibleForTesting
        double getAmountScore() {
            return amountScore;
        }

        double getScore() {
            return score;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("transaction", toJson(transaction))
                    .add("possibleMatch", possibleMatch
                            .map(FuzzyTransactionMatcher::toJson).orElse("none"))
                    .add("amountScore", amountScore)
                    .add("dateScore", dateScore)
                    .add("descriptionScore", descriptionScore)
                    .add("score", score)
                    .toString();
        }
    }

    private static String toJson(Transaction transaction) {
        return String.format("{\"userId\":\"%s\", \"credentialsId\":\"%s\", \"accountId\":\"%s\", \"date\":\"%tF\", "
                        + "\"description\":\"%s\", \"amount\":%.2f, \"pending\":%b, \"userModifiedDate\":%b, "
                        + "\"userModifiedDescription\":%b, \"userModifiedAmount\":%b}",
                transaction.getUserId(), transaction.getCredentialsId(), transaction.getAccountId(),
                transaction.getOriginalDate(), transaction.getOriginalDescription(), transaction.getOriginalAmount(),
                transaction.isPending(), transaction.isUserModifiedDate(), transaction.isUserModifiedDescription(),
                transaction.isUserModifiedAmount());
    }
}
