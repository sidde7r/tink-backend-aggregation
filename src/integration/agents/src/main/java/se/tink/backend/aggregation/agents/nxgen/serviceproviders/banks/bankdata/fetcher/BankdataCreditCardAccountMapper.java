package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import static java.util.stream.Collectors.toMap;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.LOG_TAG;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.MastercardAgreementEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.MastercardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankdataCreditCardAccountMapper {

    public static List<CreditCardAccount> getCreditCardAccounts(
            GetAccountsResponse accountsResponse) {
        return aggregateCreditCardsData(accountsResponse).stream()
                .map(BankdataCreditCardAccountMapper::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    private static CreditCardAccount toTinkCreditCard(CreditCardAggregatedData cardAggregatedData) {
        BankdataAccountEntity cardAccount = cardAggregatedData.getCardAccount();
        MastercardAgreementEntity cardAgreement = cardAggregatedData.getCardAgreement();
        MastercardEntity card = cardAggregatedData.getCard();

        String uniqueIdentifier = cardAccount.getRegNo() + cardAccount.getAccountNo();

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(card.getCardNo())
                                .withBalance(ExactCurrencyAmount.inDKK(card.getBalance()))
                                .withAvailableCredit(ExactCurrencyAmount.inDKK(card.getAvailable()))
                                .withCardAlias(card.getCardName())
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(uniqueIdentifier)
                                .withAccountNumber(cardAccount.getAccountNo())
                                /*
                                Generally we should use the name of the account that the card is linked to, but in
                                this case the card name looks better: e.g. "Mastercard Gold" instead of "MasterCard-aftale"
                                 */
                                .withAccountName(card.getCardName())
                                .addIdentifier(new MaskedPanIdentifier(card.getCardNo()))
                                .addIdentifier(
                                        new IbanIdentifier(
                                                cardAccount.getBicSwift(), cardAccount.getIban()))
                                .build())
                .setApiIdentifier(uniqueIdentifier)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.From(cardAgreement.getCanDeposit()))
                .addHolderName(cardAccount.getAccountOwner())
                .build();
    }

    /**
     * This is how we understand the API response: - every credit card account is linked to a single
     * Mastercard agreement - there can be multiple credit cards linked to the same Mastercard
     * agreement - from all agreements cards, there should be only one valid card.
     *
     * <p>Each entity provides some useful data, so we want to aggregate them into one object.
     */
    private static List<CreditCardAggregatedData> aggregateCreditCardsData(
            GetAccountsResponse accountsResponse) {

        Map<String, BankdataAccountEntity> cardAccountsByAccountNo =
                getCardAccountsByAccountNumber(accountsResponse);
        Map<String, MastercardAgreementEntity> cardAgreementsByAccountNo =
                getCardAgreementsByAccountNumber(accountsResponse);

        List<String> validCardAccountNumbers =
                getCommonCardAccountNumbers(
                        cardAccountsByAccountNo.keySet(), cardAgreementsByAccountNo.keySet());

        return validCardAccountNumbers.stream()
                .map(
                        cardNumber -> {
                            BankdataAccountEntity cardAccount =
                                    cardAccountsByAccountNo.get(cardNumber);
                            MastercardAgreementEntity cardAgreement =
                                    cardAgreementsByAccountNo.get(cardNumber);

                            List<MastercardEntity> validCards =
                                    cardAgreement.getMastercardCards().stream()
                                            .filter(BankdataCreditCardAccountMapper::isValidCard)
                                            .collect(Collectors.toList());
                            if (validCards.isEmpty()) {
                                log.warn("{} No valid cards for agreement", LOG_TAG);
                                return Optional.<CreditCardAggregatedData>empty();
                            }
                            if (validCards.size() > 1) {
                                log.warn("{} Multiple valid cards for agreement", LOG_TAG);
                            }

                            return Optional.of(
                                    CreditCardAggregatedData.builder()
                                            .cardAccount(cardAccount)
                                            .cardAgreement(cardAgreement)
                                            .card(validCards.get(0))
                                            .build());
                        })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Map<String, BankdataAccountEntity> getCardAccountsByAccountNumber(
            GetAccountsResponse accountsResponse) {
        return accountsResponse.getAccounts().stream()
                .filter(entity -> entity.getAccountType() == AccountTypes.CREDIT_CARD)
                .collect(
                        toMap(
                                BankdataAccountEntity::getAccountNo,
                                Function.identity(),
                                (a1, a2) -> {
                                    log.warn("{} Ignoring duplicated card account number", LOG_TAG);
                                    return a1;
                                }));
    }

    private static Map<String, MastercardAgreementEntity> getCardAgreementsByAccountNumber(
            GetAccountsResponse accountsResponse) {
        return accountsResponse.getMastercardAgreements().stream()
                .collect(
                        toMap(
                                MastercardAgreementEntity::getAccountNo,
                                Function.identity(),
                                (a1, a2) -> {
                                    log.warn(
                                            "{} Ignoring duplicated Mastercard agreement number",
                                            LOG_TAG);
                                    return a1;
                                }));
    }

    private static List<String> getCommonCardAccountNumbers(
            Set<String> cardAccountsNumbers, Set<String> cardAgreementsAccountNumbers) {

        Set<String> accountsWithoutAgreement =
                Sets.difference(cardAccountsNumbers, cardAgreementsAccountNumbers);
        if (!accountsWithoutAgreement.isEmpty()) {
            log.warn(
                    "{} Some card accounts have no agreements! Count: {}",
                    LOG_TAG,
                    accountsWithoutAgreement.size());
        }

        Set<String> agreementsWithoutAccount =
                Sets.difference(cardAgreementsAccountNumbers, cardAccountsNumbers);
        if (!accountsWithoutAgreement.isEmpty()) {
            log.warn(
                    "{} Some card agreements are not linked to any account! Count: {}",
                    LOG_TAG,
                    agreementsWithoutAccount.size());
        }

        return new ArrayList<>(
                Sets.intersection(cardAccountsNumbers, cardAgreementsAccountNumbers));
    }

    private static boolean isValidCard(MastercardEntity mastercardEntity) {
        String agreementAccountOwner = mastercardEntity.getAgreementAccountOwner();
        String cardUser = mastercardEntity.getCardUser();

        if (agreementAccountOwner == null || !agreementAccountOwner.equalsIgnoreCase(cardUser)) {
            log.info("{} Invalid card owner name", LOG_TAG);
            return false;
        }
        if (!isMarketCurrency(mastercardEntity.getBalanceCurrency())) {
            log.info("{} Invalid card market currency", LOG_TAG);
            return false;
        }
        return !mastercardEntity.isStopped();
    }

    private static boolean isMarketCurrency(String currency) {
        return BankdataConstants.MARKET_CURRENCY.equalsIgnoreCase(currency);
    }

    @Getter
    @Builder
    private static class CreditCardAggregatedData {
        private final BankdataAccountEntity cardAccount;
        private final MastercardAgreementEntity cardAgreement;
        private final MastercardEntity card;
    }
}
