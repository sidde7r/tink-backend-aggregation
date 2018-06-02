package se.tink.backend.system.controllers.abnamro;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.core.AbnAmroBufferedAccount;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.exceptions.RejectedAccountException;
import se.tink.libraries.abnamro.client.exceptions.SubscriptionException;
import se.tink.libraries.abnamro.client.model.RejectedContractEntity;
import se.tink.libraries.abnamro.client.rpc.SubscriptionAccountsRequest;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class AbnAmroController {
    private static final LogUtils log = new LogUtils(AbnAmroController.class);

    private final AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository;
    private final IBSubscriptionClient subscriptionClient;

    @Inject
    public AbnAmroController(AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository,
            AbnAmroConfiguration configuration, MetricRegistry metricRegistry) {
        this.abnAmroBufferedAccountRepository = abnAmroBufferedAccountRepository;
        this.subscriptionClient = new IBSubscriptionClient(configuration, metricRegistry);
    }

    public boolean subscribeAccount(Credentials credentials, Account account) {
        if (!Objects.equals(credentials.getProviderName(), AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2)) {
            log.debug("Ignoring subscriptions for old ABN AMRO providers.");
            return false;
        }

        final String bcNumber = credentials.getPayload();

        Preconditions.checkState(AbnAmroUtils.isValidBcNumberFormat(bcNumber), "BcNumber is not in a valid format.");

        AbnAmroBufferedAccount bufferedAccount = null;

        // Don't create the buffered accounts for credit cards
        if (account.getType() != AccountTypes.CREDIT_CARD) {
            bufferedAccount = AbnAmroBufferedAccount.create(credentials.getId(), account.getBankId());
            abnAmroBufferedAccountRepository.save(bufferedAccount);
        }

        try {
            subscribeAccountAtAbnAmro(bcNumber, account.getBankId());
            return true;
        } catch (SubscriptionException e) {
            markBufferedAccountAsComplete(bufferedAccount);
            AbnAmroUtils.markAccountAsFailed(account);
            return false;
        } catch (RejectedAccountException e) {
            markBufferedAccountAsComplete(bufferedAccount);
            AbnAmroUtils.markAccountAsRejected(account, e.getRejectedReasonCode());
            return false;
        }
    }

    private void markBufferedAccountAsComplete(AbnAmroBufferedAccount bufferedAccount) {
        if (bufferedAccount != null) {
            bufferedAccount.setComplete(true);
            abnAmroBufferedAccountRepository.save(bufferedAccount);
        }
    }

    private void subscribeAccountAtAbnAmro(String bcNumber, String bankId) throws SubscriptionException,
            RejectedAccountException {
        SubscriptionAccountsRequest subscriptionRequest = new SubscriptionAccountsRequest();
        subscriptionRequest.setBcNumber(bcNumber);
        subscriptionRequest.setContracts(Lists.newArrayList(Long.valueOf(bankId)));

        List<RejectedContractEntity> failures = subscriptionClient.subscribeAccountsWithoutSession(subscriptionRequest);

        if (!failures.isEmpty()) {
            throw new RejectedAccountException(failures.get(0).getRejectedReasonCode());
        }
    }
}
