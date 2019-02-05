package se.tink.backend.aggregation.agents.banks.nordea;

import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.nordea.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.CreatePaymentIn;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.CreatePaymentOut;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.aggregation.log.AggregationLogger;

public class NordeaTransferUtils {
    private static final AggregationLogger log = new AggregationLogger(NordeaTransferUtils.class);
    private static final NordeaAccountIdentifierFormatter NORDEA_ACCOUNT_IDENTIFIER_FORMATTER =
            new NordeaAccountIdentifierFormatter();

    /**
     * Side-effect: Logs all precondition fails so that we can trace down problems parsing data
     * (we should find only one match)
     *
     * @return One exclusive payment entity that matches the request we did
     */
    public static Optional<PaymentEntity> getSingleMatchingPaymentEntity(Iterable<PaymentEntity> unsignedPayments,
            TransferRequest paymentAddedRequest, TransferResponse paymentAddedResponse, AccountIdentifier toAccountIdentifier) {
        List<PaymentEntity> matches = Lists.newArrayList();
        List<IllegalStateException> notMatchingExceptions = Lists.newArrayList();

        for (PaymentEntity possibleMatch : unsignedPayments) {
            try {
                ensurePaymentMatches(possibleMatch, paymentAddedRequest, paymentAddedResponse, toAccountIdentifier);
                matches.add(possibleMatch);
            } catch (IllegalStateException notMatchingException) {
                notMatchingExceptions.add(notMatchingException);
            }
        }

        if (!Objects.equal(matches.size(), 1)) {
            // We should never end up here, but to be sure we log all exceptions thrown so we can track it down
            if (matches.size() > 1) {
                log.error(String.format("Found multiple matching payments: %s", matches));
            } else {
                for (IllegalStateException notMatchingException : notMatchingExceptions) {
                    log.error("Payment entity that doesn't match", notMatchingException);
                }
            }
            return Optional.empty();
        }

        return Optional.of(matches.get(0));
    }

    /**
     * @throws IllegalStateException if not matching or some variable is null at some place
     */
    private static void ensurePaymentMatches(PaymentEntity possibleMatch, TransferRequest paymentAddedRequest,
            TransferResponse paymentAddedResponse, AccountIdentifier toAccountIdentifier) throws IllegalStateException {
        Preconditions.checkState(!Objects.equal(possibleMatch, null));
        Preconditions.checkState(!Objects.equal(paymentAddedRequest, null));
        Preconditions.checkState(!Objects.equal(paymentAddedResponse, null));

        CreatePaymentIn createPaymentIn = paymentAddedRequest.getCreatePaymentIn();
        Preconditions.checkState(!Objects.equal(createPaymentIn, null));

        CreatePaymentOut createPaymentOut = paymentAddedResponse.getCreatePaymentOut();
        Preconditions.checkState(!Objects.equal(createPaymentOut, null));

        Preconditions.checkState(Objects.equal(createPaymentIn.getPaymentSubType(), possibleMatch.getPaymentSubType()));
        Preconditions.checkState(isSameToAccountId(possibleMatch, createPaymentIn, toAccountIdentifier));
        Preconditions.checkState(Objects.equal(createPaymentIn.getCurrency(), possibleMatch.getCurrency()));
        Preconditions.checkState(isSameAmount(possibleMatch, createPaymentIn));
        Preconditions.checkState(isSameTimePaymentDate(createPaymentOut, possibleMatch));
    }

    /**
     * Compares first if the CreatePaymentIn and PaymentEntity has same toAccountId.
     *
     * In some cases Nordea formats the account identifier to their own padded format. In their API the beneficiary
     * entities that we use to create a CreatePaymentIn has not been padded for e.g. Handelsbanken so in those cases
     * we would fail if we did not have a backup solution for it. In those cases the toAccountIdentifier (from our
     * transfer object) should have the same format if a NordeaAccountIdentifierFormatter is applied to it.
     */
    private static boolean isSameToAccountId(PaymentEntity possibleMatch, CreatePaymentIn createPaymentIn,
            AccountIdentifier toAccountIdentifier) {
        if (Objects.equal(createPaymentIn.getToAccountId(), possibleMatch.getToAccountId())) {
            return true;
        } else {
            if (toAccountIdentifier == null) {
                return false;
            }

            String nordeaFormattedIdentifier = toAccountIdentifier.getIdentifier(NORDEA_ACCOUNT_IDENTIFIER_FORMATTER);
            return Objects.equal(nordeaFormattedIdentifier, possibleMatch.getToAccountId());
        }
    }

    private static boolean isSameAmount(PaymentEntity possibleMatch, CreatePaymentIn createPaymentIn) {
        double createPaymentInAmount = Double.parseDouble(createPaymentIn.getAmount());
        double possibleMatchAmount = Double.parseDouble(possibleMatch.getAmount());
        return Objects.equal(createPaymentInAmount, possibleMatchAmount);
    }

    /**
     * Compares the date for the possible match and the payment we created
     *
     * Example for paymentDate on out vs possibleMatch:
     * createPaymentOut: 2016-03-10T12:00:00.023+01:00
     * possibleMatch: 2016-03-10T12:00:00.163+01:00
     *
     * Compared parts:
     * createPaymentOut: 2016-03-10T12:00:00
     * possibleMatch: 2016-03-10T12:00:00
     *
     * Since the MS part of the date differs, we only consider the date, hour, minutes and seconds (time zone seems very
     * unlikely to differ between any transfers made for the same account)
     */
    private static boolean isSameTimePaymentDate(CreatePaymentOut createPaymentOut, PaymentEntity possibleMatch) {
        String createPaymentOutPaymentDate = createPaymentOut.getPaymentDate();
        String possibleMatchPaymentDate = possibleMatch.getPaymentDate();

        Preconditions.checkState(createPaymentOutPaymentDate.length() >= 19);
        Preconditions.checkState(possibleMatchPaymentDate.length() >= 19);

        String createPaymentOutDateWithoutMS = createPaymentOutPaymentDate.substring(0, 19);
        String possibleMatchPaymentDateWithoutMS = possibleMatchPaymentDate.substring(0, 19);

        return Objects.equal(createPaymentOutDateWithoutMS, possibleMatchPaymentDateWithoutMS);
    }
}
