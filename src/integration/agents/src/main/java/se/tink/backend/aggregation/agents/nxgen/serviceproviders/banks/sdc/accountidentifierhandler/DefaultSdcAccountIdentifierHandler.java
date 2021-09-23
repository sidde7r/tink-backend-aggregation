package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.iban.IbanConverter;

public class DefaultSdcAccountIdentifierHandler implements SdcAccountIdentifierHandler {

    private static final String RAW_ACCOUNT_ILLEGAL_FORMAT_EXCEPTION_MSG =
            "Given account number has illegal format: ";

    private static final String BANK_ID_ILLEGAL_FORMAT_EXCEPTION_MSG =
            "Given account number has illegal format: ";

    private static final String ACCOUNT_NUMBER_ILLEGAL_FORMAT_EXCEPTION_MSG =
            "Given account number has illegal format: ";

    private final int targetBankIdLength;
    private final int targetAccountNumberLength;
    private final String isoAlpha2CountryCode;

    public static final SdcAccountIdentifierHandler DK_ACCOUNT_IDENTIFIER_HANDLER =
            new DefaultSdcAccountIdentifierHandler(4, 10, "DK");

    public static final SdcAccountIdentifierHandler FO_ACCOUNT_IDENTIFIER_HANDLER =
            new DefaultSdcAccountIdentifierHandler(4, 10, "FO");

    public static final SdcAccountIdentifierHandler NO_ACCOUNT_IDENTIFIER_HANDLER =
            new DefaultSdcAccountIdentifierHandler(4, 7, "NO");

    private DefaultSdcAccountIdentifierHandler(
            final int targetBankIdLength,
            final int targetAccountNumberLength,
            final String isoAlpha2CountryCode) {

        this.targetBankIdLength = targetBankIdLength;
        this.targetAccountNumberLength = targetAccountNumberLength;
        this.isoAlpha2CountryCode = isoAlpha2CountryCode;
    }

    @Override
    public String convertToIban(final String rawAccountNumber) {
        final String bban = getBban(rawAccountNumber);
        return IbanConverter.getIban(isoAlpha2CountryCode, bban);
    }

    @Override
    public List<AccountIdentifier> getIdentifiers(final String rawAccountNumber) {
        return ImmutableList.of(
                new BbanIdentifier(getBban(rawAccountNumber)),
                new IbanIdentifier(convertToIban(rawAccountNumber)));
    }

    private String getBban(String rawAccountNumber) {
        return isBban(rawAccountNumber) ? rawAccountNumber : convertToBban(rawAccountNumber);
    }

    private boolean isBban(final String rawAccountNumber) {
        return !rawAccountNumber.contains(".")
                && rawAccountNumber.length() == (targetBankIdLength + targetAccountNumberLength);
    }

    private String convertToBban(final String rawAccountNumber) {
        validateRawAccountNumber(rawAccountNumber);

        final String normalizedBankIdentifier = normalizeBankIdentifier(rawAccountNumber);

        final String normalizedAccountNo = normalizeAccountNo(rawAccountNumber);

        return normalizedBankIdentifier + normalizedAccountNo;
    }

    private void validateRawAccountNumber(final String rawAccountNumber) {
        final String[] parts = rawAccountNumber.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    RAW_ACCOUNT_ILLEGAL_FORMAT_EXCEPTION_MSG + rawAccountNumber);
        }

        if (!containsDigitsOnly(parts[0]) || parts[0].length() > targetBankIdLength) {
            throw new IllegalArgumentException(
                    BANK_ID_ILLEGAL_FORMAT_EXCEPTION_MSG + rawAccountNumber);
        }

        if (!containsDigitsOnly(parts[1]) || parts[1].length() > targetAccountNumberLength) {
            throw new IllegalArgumentException(
                    ACCOUNT_NUMBER_ILLEGAL_FORMAT_EXCEPTION_MSG + rawAccountNumber);
        }
    }

    private String normalizeBankIdentifier(final String rawAccountNumber) {
        final String[] parts = rawAccountNumber.split("\\.");
        return prefixWithZeros(parts[0], targetBankIdLength);
    }

    private String normalizeAccountNo(final String rawAccountNumber) {
        final String[] parts = rawAccountNumber.split("\\.");
        return prefixWithZeros(parts[1], targetAccountNumberLength);
    }
}
