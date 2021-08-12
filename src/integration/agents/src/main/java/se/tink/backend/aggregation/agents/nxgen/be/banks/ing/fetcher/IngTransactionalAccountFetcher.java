package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Types;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngProxyApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.AgreementEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.AgreementsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngMiscUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class IngTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngProxyApiClient ingProxyApiClient;

    public IngTransactionalAccountFetcher(IngProxyApiClient ingProxyApiClient) {
        this.ingProxyApiClient = ingProxyApiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AgreementsResponseEntity agreements = ingProxyApiClient.getAgreements(Types.ALL);
        log.info("Agreements entity: {}", agreements);
        return agreements.getAgreements().stream()
                .filter(agreement -> Types.SAVINGS.equals(agreement.getType()))
                .map(this::map)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> map(AgreementEntity agreementEntity) {

        return TransactionalAccount.nxBuilder()
                .withType(map(agreementEntity.getType()))
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(extractBalance(agreementEntity.getBalance()))
                                .setAvailableBalance(
                                        extractBalance(agreementEntity.getAvailableBalance()))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(agreementEntity.getCommercialId().getValue())
                                .withAccountNumber(agreementEntity.getCommercialId().getValue())
                                .withAccountName(agreementEntity.getProductName())
                                .addIdentifier(
                                        new SepaEurIdentifier(
                                                agreementEntity.getCommercialId().getValue()))
                                .build())
                .putInTemporaryStorage(
                        Storage.TRANSACTIONS_HREF,
                        extractTransactionsHref(agreementEntity.getLinks()))
                .addHolderName(agreementEntity.getHolderName())
                .build();
    }

    private TransactionalAccountType map(String type) {
        if (Types.CURRENT.equals(type)) {
            return TransactionalAccountType.CHECKING;
        } else if (Types.SAVINGS.equals(type)) {
            return TransactionalAccountType.SAVINGS;
        }
        throw new AccountRefreshException("Unknown account type " + type);
    }

    /**
     * @param amountEntity is null for Savings Account and in that situation we want to put some
     *     dummy value
     * @return Mapped value to the ExactCurrencyAmount
     */
    private ExactCurrencyAmount extractBalance(AmountEntity amountEntity) {
        return Optional.ofNullable(amountEntity)
                .map(entity -> ExactCurrencyAmount.of(entity.getValue(), entity.getCurrency()))
                .orElse(ExactCurrencyAmount.zero("EUR"));
    }

    private String extractTransactionsHref(List<LinkEntity> links) {
        return links.stream()
                .filter(link -> "transactions".equals(link.getRel()))
                .findFirst()
                .map(link -> IngMiscUtils.decodeUrl(link.getHref()))
                .orElseThrow(() -> new IllegalStateException("No transactions ref for account"));
    }
}
