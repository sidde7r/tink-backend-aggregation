package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskAccountTypeMappers;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInfo;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInformationListItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountInformationListResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

public final class GetAccountInformationListResponse {
    private static final Logger logger =
            LoggerFactory.getLogger(GetAccountInformationListResponse.class);

    private static final BawagPskAccountTypeMappers mappers = new BawagPskAccountTypeMappers();

    private Envelope envelope;

    public GetAccountInformationListResponse(Envelope envelope) {
        this.envelope = envelope;
    }

    /** @return A collection of invalid IBANs for the accounts that have one. */
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
        final Optional<OK> ok =
                Optional.ofNullable(envelope)
                        .map(Envelope::getBody)
                        .map(Body::getGetAccountInformationListResponseEntity)
                        .map(GetAccountInformationListResponseEntity::getOk);

        if (!ok.isPresent()) {
            logger.error(
                    "{} - Did not receive an OK response in account fetching response",
                    BawagPskConstants.LogTags.RESPONSE_NOT_OK.toTag());
        }

        return ok.map(OK::getAccountInformationListItemList)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(Collection::stream) // Stream<Collection<T>> -> Stream<T>
                .filter(Objects::nonNull)
                .map(AccountInformationListItem::getAccountInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static IbanIdentifier getIban(final ProductID productID) {
        return new IbanIdentifier(
                productID.getFinancialInstitute().getBIC().trim(), productID.getIban().trim());
    }

    // Too dumb and lazy to find a way to eliminate these dupes
    public Collection<TransactionalAccount> extractTransactionalAccounts(
            final Map<String, String> productCodes) {
        return getAccountInfoList().stream()
                .map(
                        accInfo ->
                                toTransactionalAccount(
                                        accInfo, productCodes.get(accInfo.getAccountNumber())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public Collection<CreditCardAccount> extractCreditCardAccounts(
            final Map<String, String> productCodes) {
        return getAccountInfoList().stream()
                .map(
                        accInfo ->
                                toCreditCardAccount(
                                        accInfo, productCodes.get(accInfo.getAccountNumber())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public Collection<LoanAccount> extractLoanAccounts(final Map<String, String> productCodes) {
        return getAccountInfoList().stream()
                .map(
                        accInfo ->
                                toLoanAccount(
                                        accInfo, productCodes.get(accInfo.getAccountNumber())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public Collection<InvestmentAccount> extractInvestmentAccounts(
            final Map<String, String> productCodes) {
        return getAccountInfoList().stream()
                .map(
                        accInfo ->
                                toInvestmentAccount(
                                        accInfo, productCodes.get(accInfo.getAccountNumber())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<TransactionalAccount> toTransactionalAccount(
            final AccountInfo accountInfo, final String productCode) {
        return mappers.inferAccountType(productCode, accountInfo.getProductID().getProductType())
                .filter(type -> type == AccountTypes.CHECKING || type == AccountTypes.SAVINGS)
                .map(
                        type ->
                                TransactionalAccount.builder(
                                                type,
                                                accountInfo.getAccountNumber(),
                                                new Amount(
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getCurrency(),
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getAmount()))
                                        .setAccountNumber(accountInfo.getAccountNumber())
                                        .addIdentifier(getIban(accountInfo.getProductID()))
                                        .setBankIdentifier(accountInfo.getAccountNumber())
                                        .setHolderName(
                                                new HolderName(
                                                        accountInfo
                                                                .getProductID()
                                                                .getAccountOwner()
                                                                .trim()))
                                        .build());
    }

    private static Optional<CreditCardAccount> toCreditCardAccount(
            final AccountInfo accountInfo, final String productCode) {

        return mappers.inferAccountType(productCode, accountInfo.getProductID().getProductType())
                .filter(type -> type == AccountTypes.CREDIT_CARD)
                .map(
                        type ->
                                CreditCardAccount.builder(
                                                accountInfo.getAccountNumber(),
                                                new Amount(
                                                        accountInfo
                                                                .getCurrentSaldoEntity()
                                                                .getCurrency(),
                                                        accountInfo
                                                                .getCurrentSaldoEntity()
                                                                .getAmount()),
                                                new Amount(
                                                        accountInfo
                                                                .getDisposableBalanceEntity()
                                                                .getCurrency(),
                                                        accountInfo
                                                                .getDisposableBalanceEntity()
                                                                .getAmount()))
                                        .setAccountNumber(accountInfo.getAccountNumber())
                                        .addIdentifier(getIban(accountInfo.getProductID()))
                                        .setBankIdentifier(accountInfo.getAccountNumber())
                                        .setHolderName(
                                                new HolderName(
                                                        accountInfo
                                                                .getProductID()
                                                                .getAccountOwner()
                                                                .trim()))
                                        .build());
    }

    private static Optional<LoanAccount> toLoanAccount(
            final AccountInfo accountInfo, final String productCode) {

        return mappers.inferAccountType(productCode, accountInfo.getProductID().getProductType())
                .filter(type -> type == AccountTypes.LOAN)
                .map(
                        type ->
                                LoanAccount.builder(
                                                accountInfo.getAccountNumber(),
                                                new Amount(
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getCurrency(),
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getAmount()))
                                        .setAccountNumber(accountInfo.getAccountNumber())
                                        .addIdentifier(getIban(accountInfo.getProductID()))
                                        .setBankIdentifier(accountInfo.getAccountNumber())
                                        .setHolderName(
                                                new HolderName(
                                                        accountInfo
                                                                .getProductID()
                                                                .getAccountOwner()
                                                                .trim()))
                                        .build());
    }

    private static Optional<InvestmentAccount> toInvestmentAccount(
            final AccountInfo accountInfo, final String productCode) {
        return mappers.inferAccountType(productCode, accountInfo.getProductID().getProductType())
                .filter(type -> type == AccountTypes.INVESTMENT)
                .map(
                        type ->
                                InvestmentAccount.builder(accountInfo.getAccountNumber())
                                        // Need to set balance to prevent NPE
                                        .setBalance(
                                                new Amount(
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getCurrency(),
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getAmount()))
                                        .setCashBalance(
                                                new Amount(
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getCurrency(),
                                                        accountInfo
                                                                .getCurrentBalanceEntity()
                                                                .getAmount()))
                                        .setPortfolios(Collections.emptyList())
                                        .setAccountNumber(accountInfo.getAccountNumber())
                                        .addIdentifier(getIban(accountInfo.getProductID()))
                                        .setBankIdentifier(accountInfo.getAccountNumber())
                                        .setHolderName(
                                                new HolderName(
                                                        accountInfo
                                                                .getProductID()
                                                                .getAccountOwner()
                                                                .trim()))
                                        .build());
    }
}
