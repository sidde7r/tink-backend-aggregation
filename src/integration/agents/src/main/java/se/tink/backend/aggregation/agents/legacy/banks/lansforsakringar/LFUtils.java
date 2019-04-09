package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.EInvoice;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TransferRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form.AnswerEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.date.DateUtils;

public class LFUtils {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    private static final Pattern MONTHS_BOUND = Pattern.compile("(\\d+) MÅNADER");
    private static final Pattern YEARS_BOUND = Pattern.compile("(\\d+) ÅR");

    /**
     * List of banks that should not have clearing included when doing transfers ...based on
     * response from LFs API call that lists BankEntities
     */
    private static final ImmutableList<ClearingNumber.Bank> BANKS_THAT_SHOULD_NOT_HAVE_CLEARING =
            ImmutableList.of(
                    ClearingNumber.Bank.EKOBANKEN,
                    ClearingNumber.Bank.HANDELSBANKEN,
                    ClearingNumber.Bank.JAKBANKEN,
                    ClearingNumber.Bank.PLUSGIROT,
                    ClearingNumber.Bank.SPARBANKEN_SYD);

    public static Optional<AccountEntity> find(
            final AccountIdentifier identifier, List<AccountEntity> entities) {
        return entities.stream()
                .filter(
                        ae ->
                                (equal(
                                        ae.getAccountNumber(),
                                        identifier.getIdentifier(DEFAULT_FORMATTER))))
                .findFirst();
    }

    public static Predicate<EInvoice> findEInvoice(final String electronicInvoiceId) {
        return input -> Objects.equals(input.getElectronicInvoiceId(), electronicInvoiceId);
    }

    /** Converts a map from supplemental information into a list of answer entities. */
    public static List<AnswerEntity> convertSupplementalInformationToAnswers(
            Map<String, Object> input) {
        List<AnswerEntity> answers = Lists.newArrayList();

        if (input == null) {
            return answers;
        }

        for (String questionId : input.keySet()) {
            answers.add(new AnswerEntity(questionId, input.get(questionId)));
        }

        return answers;
    }

    public static Integer parseNumMonthsBound(String bound) {
        // "rateBindingPeriodLength": "3 MÅNADER"
        // guessing it is "x ÅR" for years

        Matcher mMonths = MONTHS_BOUND.matcher(bound);
        if (mMonths.matches()) {
            return Integer.parseInt(mMonths.group(1));
        }

        Matcher mYears = YEARS_BOUND.matcher(bound);
        if (mYears.matches()) {
            return Integer.parseInt(mYears.group(1)) * 12;
        }
        return null;
    }

    /**
     * LF requires that the account number is given without clearing number for some banks (e.g.
     * Handelsbanken). Note: Nordea SSN accounts are special compared to the rest of Nordea
     * according to LF list of banks
     *
     * @return Either account identifier with or without clearing depending on bank according to LF
     *     requirements
     */
    public static String getApiAdaptedToAccount(SwedishIdentifier destination) {
        ClearingNumber.Details clearingNumberDetails = getClearingNumberDetails(destination);

        ClearingNumber.Bank destinationBank = clearingNumberDetails.getBank();
        if (BANKS_THAT_SHOULD_NOT_HAVE_CLEARING.contains(destinationBank)) {
            return destination.getAccountNumber();
        } else if (Objects.equals(destinationBank, ClearingNumber.Bank.NORDEA_PERSONKONTO)) {
            return destination.getAccountNumber();
        } else {
            return destination.getIdentifier(DEFAULT_FORMATTER);
        }
    }

    public static ClearingNumber.Details getClearingNumberDetails(SwedishIdentifier destination) {
        String clearingNumber = destination.getClearingNumber();
        Optional<ClearingNumber.Details> clearingNumberDetails = ClearingNumber.get(clearingNumber);

        if (!clearingNumberDetails.isPresent()) {
            String errorMessage =
                    String.format(
                            "Unexpectedly no clearingnumber for destination: %s (%s, %s)",
                            destination.getIdentifier(),
                            destination.getClearingNumber(),
                            destination.getIdentifier(new DisplayAccountIdentifierFormatter()));
            throw new NullPointerException(errorMessage);
        }
        return clearingNumberDetails.get();
    }

    /**
     * Compare payment with LF entities
     *
     * <p>If the original payments dueDate is null, we set paymentDate on the PaymentRequest to 0 in
     * order for LF to chose the earliest possible payment date. However, if we need to remove the
     * payment from LF we need to compare it to the PaymentEntity object which will contain the
     * paymentDate LF selected ( NOT 0 ). So in order to match identify the PaymentEntity we need to
     * ignore dates when matching a PaymentRequest with a PaymentEntity where the paymentDate on the
     * PaymentRequest is 0.
     *
     * <p>Otherwise we won't match the payments and the payment won't be removed from the bank.
     */
    public static boolean isSamePayment(PaymentRequest paymentRequest, Object obj) {
        long requestDate = flattenDate(paymentRequest.getPaymentDate());
        String requestHash = paymentRequest.calculateHash();

        if (obj instanceof PaymentEntity) {
            PaymentEntity paymentEntity = (PaymentEntity) obj;
            long entityDate = flattenDate(paymentEntity.getDate());

            return Objects.equals(requestHash, paymentEntity.calculateHash())
                    && (paymentRequest.getPaymentDate() == 0
                            || Objects.equals(requestDate, entityDate));
        } else if (obj instanceof UpcomingTransactionEntity) {
            UpcomingTransactionEntity transaction = (UpcomingTransactionEntity) obj;

            if (transaction.getPaymentInfo() == null) {
                return false;
            }

            long transactionDate = flattenDate(transaction.getDate().getTime());

            return Objects.equals(
                                    requestHash,
                                    transaction.calculatePaymentHash(
                                            paymentRequest.getFromAccount()))
                            && paymentRequest.getPaymentDate() == 0
                    || Objects.equals(requestDate, transactionDate);
        }

        return false;
    }

    public static boolean isSameTransfer(
            TransferRequest transferRequest, UpcomingTransactionEntity upcomingTransactionEntity) {

        if (upcomingTransactionEntity.getTransferInfo() == null) {
            return false;
        }

        return Objects.equals(
                transferRequest.calculateHash(),
                upcomingTransactionEntity.calculateTransferHash(transferRequest.getFromAccount()));
    }

    private static long flattenDate(long date) {
        return DateUtils.flattenTime(new Date(date)).getTime();
    }
}
