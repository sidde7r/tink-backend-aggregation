package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount;

import java.security.interfaces.RSAPrivateKey;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.SignatureValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration.FiduciaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.JWTUtils;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.SignatureUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FiduciaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final FiduciaApiClient apiClient;
    private final FiduciaConfiguration configuration;
    private final String certificate;
    private final String keyId;
    private final RSAPrivateKey privateKey;

    public FiduciaTransactionalAccountFetcher(
            FiduciaApiClient apiClient, FiduciaConfiguration configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;

        certificate = JWTUtils.readFile(configuration.getCertificatePath());
        keyId = configuration.getKeyId();
        privateKey = JWTUtils.getKey(configuration.getKeyPath());
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String digest = SignatureUtils.createDigest(SignatureValues.EMPTY_BODY);
        String date = SignatureUtils.getCurrentDateFormatted();
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                SignatureUtils.createSignature(
                        privateKey, keyId, SignatureValues.HEADERS, digest, reqId, date, null);

        GetAccountsResponse getAccountsResponse =
                apiClient.getAccounts(digest, certificate, signature, reqId, date);
        getAccountsResponse
                .getAccounts()
                .forEach(
                        acc -> {
                            String balancesDate = SignatureUtils.getCurrentDateFormatted();
                            String balancesReqId = String.valueOf(UUID.randomUUID());
                            String balancesSignature =
                                    SignatureUtils.createSignature(
                                            privateKey,
                                            keyId,
                                            SignatureValues.HEADERS,
                                            digest,
                                            balancesReqId,
                                            balancesDate,
                                            null);

                            GetBalancesResponse getBalancesResponse =
                                    apiClient.getBalances(
                                            acc,
                                            digest,
                                            certificate,
                                            balancesSignature,
                                            balancesReqId,
                                            balancesDate);
                            acc.setBalances(getBalancesResponse.getBalances());
                        });

        return getAccountsResponse.toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String digest = SignatureUtils.createDigest(SignatureValues.EMPTY_BODY);
        String date = SignatureUtils.getCurrentDateFormatted();
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                SignatureUtils.createSignature(
                        privateKey, keyId, SignatureValues.HEADERS, digest, reqId, date, null);

        return apiClient.getTransactions(
                account, fromDate, toDate, digest, certificate, signature, reqId, date);
    }
}
