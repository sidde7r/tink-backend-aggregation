package se.tink.backend.aggregation.agents.abnamro.ics;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.IcsException;
import se.tink.backend.aggregation.agents.abnamro.client.model.ErrorEntity;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.strings.StringUtils;

/** Build a user friendly error message for ICS credit cards. */
public class ErrorMessageBuilder {

    private final SortedSetMultimap<ErrorType, Long> contractNumbersByErrorType;
    private final Catalog catalog;

    private enum ErrorType {
        TEMPORARY_ERROR,
        APPROVAL_ERROR
    }

    public ErrorMessageBuilder(Catalog catalog) {
        this.contractNumbersByErrorType = TreeMultimap.create();
        this.catalog = catalog;
    }

    public void addException(Long contractNumber, Exception exception) {

        Throwable toCheck = exception;

        // Match against the cause if there is an ExecutionException
        if (exception instanceof ExecutionException) {
            ExecutionException executionException = (ExecutionException) exception;

            if (executionException.getCause() != null) {
                toCheck = executionException.getCause();
            }
        }

        if (toCheck instanceof IcsException) {
            IcsException icsException = (IcsException) toCheck;
            contractNumbersByErrorType.put(
                    mapErrorKeyToErrorType(icsException.getKey()), contractNumber);
        } else {
            contractNumbersByErrorType.put(ErrorType.TEMPORARY_ERROR, contractNumber);
        }
    }

    public boolean hasExceptions() {
        return !contractNumbersByErrorType.isEmpty();
    }

    private ErrorType mapErrorKeyToErrorType(String errorKey) {
        if (ErrorEntity.APPROVAL_ERRORS.contains(errorKey)) {
            return ErrorType.APPROVAL_ERROR;
        }

        return ErrorType.TEMPORARY_ERROR;
    }

    public String build() {

        List<String> errors = Lists.newArrayList();

        for (ErrorType errorType : contractNumbersByErrorType.keySet()) {

            Set<Long> contractNumbers = contractNumbersByErrorType.get(errorType);

            if (errorType == ErrorType.TEMPORARY_ERROR) {
                errors.add(getTranslatedTemporaryError(contractNumbers));
            } else if (errorType == ErrorType.APPROVAL_ERROR) {
                errors.add(getTranslatedApprovalError(contractNumbers));
            }
        }

        return Joiner.on("\n\n").join(errors);
    }

    private String getTranslatedApprovalError(Set<Long> contractNumbers) {

        String format =
                catalog.getPluralString(
                        "No transactions are available for credit card {0}. Please go to ‘Settings’ > ‘Accounts’ in the Mobile Banking app to add this credit card. If your credit card transactions are displayed in Mobile Banking, they will also be available for Grip.",
                        "No transactions are available for credit cards {0}. Please go to ‘Settings’ > ‘Accounts’ in the Mobile Banking app to add these credit cards. If your credit cards transactions are displayed in Mobile Banking, they will also be available for Grip.",
                        contractNumbers.size());

        return Catalog.format(format, formatContractNumbers(contractNumbers));
    }

    private String getTranslatedTemporaryError(Set<Long> contractNumbers) {
        String format =
                catalog.getPluralString(
                        "A temporary error occurred in the communication between ABN and ICS for the credit card {0}. We will automatically refresh data as soon as the connection is available.",
                        "A temporary error occurred in the communication between ABN and ICS for the credit cards {0}. We will automatically refresh data as soon as the connection is available.",
                        contractNumbers.size());

        return Catalog.format(format, formatContractNumbers(contractNumbers));
    }

    private String formatContractNumbers(Set<Long> contractNumbers) {
        ImmutableList<String> list =
                FluentIterable.from(contractNumbers)
                        .transform(
                                contractNumber ->
                                        AbnAmroUtils.maskCreditCardContractNumber(
                                                String.valueOf(contractNumber), true))
                        .toList();

        String firstSeparator = ", ";
        String lastSeparator = " " + catalog.getString("and") + " ";

        return StringUtils.join(list, firstSeparator, lastSeparator);
    }
}
