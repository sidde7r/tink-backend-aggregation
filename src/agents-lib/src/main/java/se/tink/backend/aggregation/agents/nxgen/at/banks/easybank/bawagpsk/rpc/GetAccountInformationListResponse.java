package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInfo;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInformationListItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountInformationListResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public final class GetAccountInformationListResponse {
    private static final Logger logger = LoggerFactory.getLogger(GetAccountInformationListResponse.class);

    private Envelope envelope;

    public GetAccountInformationListResponse(Envelope envelope) {
        this.envelope = envelope;
    }

    /**
     * @return A collection of invalid IBANs for the accounts that have one.
     */
    public Collection<IbanIdentifier> getInvalidIbans() {
        return getAccountInfoList().stream()
                .map(AccountInfo::getProductID)
                .filter(Objects::nonNull)
                .filter(productID -> productID.getFinancialInstitute() != null)
                .filter(productID -> productID.getFinancialInstitute().getBIC() != null)
                .filter(productID -> productID.getIban() != null)
                .map(GetAccountInformationListResponse::getIban)
                .filter(iban -> !iban.isValid())
                .collect(Collectors.toList());
    }

    private List<AccountInfo> getAccountInfoList() {
        final Optional<OK> ok = Optional.ofNullable(envelope)
                .map(Envelope::getBody)
                .map(Body::getGetAccountInformationListResponseEntity)
                .map(GetAccountInformationListResponseEntity::getOk);

        if (!ok.isPresent()) {
            logger.error("{} - Did not receive an OK response in account fetching response",
                    BawagPskConstants.LogTags.RESPONSE_NOT_OK.toTag());
        }

        return ok.map(OK::getAccountInformationListItemList)
                .map(Stream::of).orElse(Stream.empty()) // Optional -> Stream
                .flatMap(Collection::stream) // Stream<Collection<T>> -> Stream<T>
                .filter(Objects::nonNull)
                .map(AccountInformationListItem::getAccountInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static IbanIdentifier getIban(final ProductID productID) {
        return new IbanIdentifier(
                productID.getFinancialInstitute().getBIC().trim(),
                productID.getIban().trim()
        );
    }

    public Collection<TransactionalAccount> toTransactionalAccounts(final Map<String, String> productCodes) {
        return getAccountInfoList().stream()
                .map(accountInfo -> toTransactionalAccount(
                        accountInfo,
                        productCodes.get(accountInfo.getAccountNumber())))
                .collect(Collectors.toSet());
    }

    private static TransactionalAccount toTransactionalAccount(
            final AccountInfo accountInfo,
            final String productCode) {
        return TransactionalAccount
                .builder(inferAccountType(
                        productCode,
                        accountInfo.getProductID().getProductType()),
                        accountInfo.getAccountNumber(),
                        new Amount(
                                accountInfo.getAmountEntity().getCurrency(),
                                accountInfo.getAmountEntity().getAmount()
                        )
                )
                .setAccountNumber(accountInfo.getAccountNumber())
                .addIdentifier(getIban(accountInfo.getProductID()))
                .setHolderName(new HolderName(accountInfo.getProductID().getAccountOwner().trim()))
                .build();
    }

    /**
     * It is assumed -- but not verified -- that the app infers the account type from the first character of the
     * account's product code. We cannot use <ProductType> to infer the account type because it has been shown that the
     * server incorrectly sets it to "CHECKING" even in cases where it should be "SAVINGS".
     */
    private static AccountTypes inferAccountType(final String productCode, final String productType) {
        switch (productCode.charAt(0)) {
        case 'B':
            return AccountTypes.CHECKING;
        case 'D':
            return AccountTypes.SAVINGS;
        case '0': // Observed values: "00EC", "00PD"
            return AccountTypes.CREDIT_CARD;
        case 'S': // Observed values: "S132"
            return AccountTypes.LOAN;
        default:
            logger.error(String.format(
                    "Account type could not be inferred from product code '%s'. Expected prefix B, D, S or 0.",
                    productCode));
        }

        logger.warn(String.format("Falling back to inferring from product type string '%s'.", productType));

        switch (productType.toUpperCase()) {
        case "CHECKING":
            return AccountTypes.CHECKING;
        case "SAVINGS":
            return AccountTypes.SAVINGS;
        default:
            logger.error(String.format(
                    "Account type could not be inferred from product type '%s'. Expected 'CHECKING' or 'SAVINGS'.",
                    productType));
        }
        logger.warn("Falling back to setting the product type to CHECKING");
        return AccountTypes.CHECKING;
    }
}
