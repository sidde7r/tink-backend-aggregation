package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.contexts.EidasContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.filter.ReAuthenticateFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.EidasJwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.identitydata.IdentityData;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    /*.jks file containg root UKOB issuing CA obtained from https://openbanking.atlassian.net/wiki/spaces/DZ/pages/80544075/OB+Root+and+Issuing+Certificates+for+Production*/
    private static final byte[] UKOB_ROOT_CA_JKS =
            Base64.getDecoder()
                    .decode(
                            "/u3+7QAAAAIAAAADAAAAAgAFcm9vdDEAAAFmXOF29wAFWC41MDkAAAVGMIIFQjCCAyqgAwIBAgIEWgGaAzANBgkqhkiG9w0BAQsFADBBMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHDAaBgNVBAMTE09wZW5CYW5raW5nIFJvb3QgQ0EwHhcNMTcxMTA3MTEwOTM2WhcNMzcxMTA3MTEzOTM2WjBBMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHDAaBgNVBAMTE09wZW5CYW5raW5nIFJvb3QgQ0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQDvJkaqdaIUNgTnXcJ3lKLyjhTJSsNtYzmN7fvpn8oseBQXQDKzJAvLXhfUEVeuUu3Zv/TG+ab/pSFdtiibh5PLIbB8nQDORl/fAA68wIjImsa2feUcq91Y+dKdKN8iW6zop8aDL8qwEggAV/u3TRfOhF8LSKHOEZ/7/YRTuqinAxDkeHYh7G+uSReyP4NvehhDkSuhK44zbyEddOvvcAOrkYr9TtBj6iZ5OMVZGO9tY9gRkbiQOt1FozyuYB7XT0QzokIfBWE0CZ1ypdu2bttDC7CuVhw9QSnyFHIG6HtQi2zKZH9OceMPJiG9RAdBUDZ3qqLFEVSvw1Dgfu/iatPEgYTbRDA85EHeGCcTMCTGra0eoITekrq//CRW1e73lK40SFzmMK/lKD3B2qWz/TxMvEH186s5REKPC6ptiQ4TxIp8Ls4gn2UHGwbS7i9ihryr0/ww9ILzy3gkuahf1t6PaNwmU02dovfLG5LJrMnvn8P6SdPwgbt3TtMKPBTxawQK+4N7wcY3slvh6bj9XLdyYKkqAk5QDiGoyZypZ6iH6P40gxJgJquF3kgYTSWunWkylDC6QgUU5U+x43SorH3qBB/fN5+daI8PQo80gbvonnWDAelxMkNUTkt/469CBpOd0Ok5uhl6g1cb9Tl1i3IR1c3Daa1hHK2eoKfsOMjwVwIDAQABo0IwQDAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUKnp9q41DYWT2XgHagTSVGFM8ny4wDQYJKoZIhvcNAQELBQADggIBAGX85+GSIXLJhZ6FwXZgrm+jKvdzxWP3qkwEhNmxfA3Cl4oVzINkn8fQfz3LN9zwTqRusxXfdpSdxfMesB480sDUDy88VAIdNi5A1DFFL02qZJxOH5cBRN+VVRPfRLSXK56LlbItM38GdhRVhd0FVnpG9+tqkmseF63rDCP30BOidUEH1Ong+0Bt8vZOs/OcPyGswsQJS3/7I1QFPxm/0F7wwBxdZwODcz4TAmw9EpePgNvI7ayhM7V/krMJeyG1bQ1sXu7LWdQIEEavrnV0fGgWPbG9L1QzhIxO5PzUKsA09W3wweRVQJxcYRWw3L1orwrvKZktvsKq1K7PEsIzHd3N/L+gGNDdYCZgeL+uv4aIoArPvJa06bVBSiunmkN4LuSRv0pVQPXkNzNkeTgJuCqE8DQavkjDY6OvhTjL54LGT8cv8wrgL9ZZWiol+LYABiF3ffdS7uXNAMEmHTAniBsw6t4VmoT6sjDD7Y4QLG7mJ53MIFbBb/+Y3IJQj474Yl9bOk3lbEJ8fSj1DtuRrygxDjUFZ2IqbuliLN86nN9SMIr+WZBAIG3bT3I8EkAvVPPHiWXjZZV/oBQq3C4fZT7ELu1Y2Z4h3Z/OW3/8OHbqKHnXS9MsOvJ1cVHHb/dRAeg2iKLbVikYKQM5mShYIJ0zIxKS7I/UKU5fYtfkskMiAAAAAgAFcm9vdDAAAAFmXOFDxAAFWC41MDkAAAZ2MIIGcjCCBFqgAwIBAgIEWgGaQTANBgkqhkiG9w0BAQsFADBBMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHDAaBgNVBAMTE09wZW5CYW5raW5nIFJvb3QgQ0EwHhcNMTcxMTA3MTE1MzM2WhcNMjcxMTA3MTIyMzM2WjBEMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHzAdBgNVBAMTFk9wZW5CYW5raW5nIElzc3VpbmcgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC0AjBf/+FDu9mzJjh/BYK4N7rF1ImQ490suImOkS7oSuHuI5cSfYhvgtNguIkMkcIyoBdgAcN2GdcslZwdD1lytp1QrSiveqVTrto0HS45jSu0/Y46mspNq146Ue5v7sdvnFs5WimWsOMRoUhbgrBwfumA2tyTVCzGLQ2xb8geUxav7IPA7NAQmIY+G08UAE9qkLeQliItliJDqt52tRfDzFdpL+HmmQgB7hR3nTntpjjcsVDCypUYRM1PyoxXluw6i/YT3vQ2DAkV1vg2SGi4+5A6lgXTSDGMga6PkS7P8OpFlPg0C5B6I8NvxIW3JDIkpsI5VVlutF6ISNihvFQ7AgMBAAGjggJtMIICaTAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADCCAVIGA1UdIASCAUkwggFFMIIBQQYLKwYBBAGodYEGAQEwggEwMDUGCCsGAQUFBwIBFilodHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9wb2xpY2llczCB9gYIKwYBBQUHAgIwgekMgeZUaGlzIENlcnRpZmljYXRlIGlzIHNvbGVseSBmb3IgdXNlIHdpdGggT3BlbiBCYW5raW5nIExpbWl0ZWQgYW5kIGFzc29jaWF0ZWQgT3BlbiBCYW5raW5nIFNlcnZpY2VzLiBJdHMgcmVjZWlwdCwgcG9zc2Vzc2lvbiBvciB1c2UgY29uc3RpdHV0ZXMgYWNjZXB0YW5jZSBvZiB0aGUgT3BlbiBCYW5raW5nIExpbWl0ZWQgQ2VydGlmaWNhdGUgUG9saWN5IGFuZCByZWxhdGVkIGRvY3VtZW50cyB0aGVyZWluLjBvBggrBgEFBQcBAQRjMGEwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDcGCCsGAQUFBzAChitodHRwOi8vb2IudHJ1c3Rpcy5jb20vcHJvZHVjdGlvbi9yb290Y2EuY3J0MDwGA1UdHwQ1MDMwMaAvoC2GK2h0dHA6Ly9vYi50cnVzdGlzLmNvbS9wcm9kdWN0aW9uL3Jvb3RjYS5jcmwwHwYDVR0jBBgwFoAUKnp9q41DYWT2XgHagTSVGFM8ny4wHQYDVR0OBBYEFJ9Jv042p6zDDyvIR/QfKRvAeQsFMA0GCSqGSIb3DQEBCwUAA4ICAQB0fhfsXqNc/aaWuV0gYCjJO9zhpbBdV3Q3ige7cpMGcyHM53+Ijf51zkSxRPCRMPjAC7yWPVqI3yi8iBdUSNUoUQmWy71yVfqaDtos2XPkkPsgZ5Q+wYm8/eSsCBhMC0yzVBDvOY1XrowZVRZqpywcMQQ9HFK1r9fDzIh95MW/ABE7qkTQlp0OsQwajODsuKndh1uTMFokqP+rbArqZEfHRexzhtzgRCG6T2SXl4g30SEpxZoLSbXmCgJqKlVi3BuaatqbwNM4laJnIPfNJMym+oih0ZR+sPHrdn0uWJN4PlcQfk2/2QTfvvb6jm8ntdduqZXHOpIY5k2e9nJA5ybo3PsrmiyPcLyxFo06GDLVBtmkkJDHr+ZK9v/ierQWQWC/C22RYskiSfHycX3W1hCR2Njorglgv7GbdjZ5cgCSzDNR++QR8d2qQmxzTAGTdLRXUmVkpPeib2vSp2rWeBgXg4EZiR621bVefgvsdycpT+Y0DYZHl9cfQnT4ee0L9ydzYMz6zN3RCgyERJncYjla27ENf11O8jJDnYoOPZPS6GG/sd1RpxWyPyj3lvrxb2uFnL+JA8voQMEbdmcyIv8IPglGyr75N3g4lTllidPprDook6DhsQTJJzLXjgsMYysfkyyUAR38LwhjuP6lWO6EYnWleUmyzPpMBbAfEzb5NgAAAAIAB2lzc3VpbmcAAAFmXOGuowAFWC41MDkAAAXJMIIFxTCCBK2gAwIBAgIEWf8PZDANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxHzAdBgNVBAMTFk9wZW5CYW5raW5nIElzc3VpbmcgQ0EwHhcNMTcxMjIxMTYxNzUyWhcNMTkwMTIxMTY0NzUyWjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZBgNVBAsTEjAwMTU4MDAwMDBqZndCNEFBSTEfMB0GA1UEAxMWM1o1Zk1QRU5zejA3NWhZTFU5Umx5NTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANUIFfbJIy90JLLy/cDN5OQYYDLbUWCj7SVkwOjx5oVlVvNdI/0G+k5mYRAFcaZuUHJPaA3IKxh2HGqFd8RCP1ZTaHC8VEaOU0GAmj4+3gwxNGao3vmq022bAL+RpmCxXGyhoEUaTVCVcVoZlPxtKHLQVIBrFuRDa0Jx00QoNpWAeAh7g2d+RyXe2VuDVbkFdIiPwSrAYhkVtRuLV9r+G8f2wzx+EFVc3JK8a+hiCH/BRX6e4yFhgQC0H3eICKLmtekU+vNU9htFiK/tfOBbGgq7MyMa7zrazs0yGiQIJ8VE0EOP2Z9MWnOJ05hVH409pn3dstTfJQXh6XBn++1tVqcCAwEAAaOCAqAwggKcMB0GA1UdEQQWMBSCEnNlY3VyZTF0LnJicy5jby51azAOBgNVHQ8BAf8EBAMCB4AwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIIBUgYDVR0gBIIBSTCCAUUwggFBBgsrBgEEAah1gQYBATCCATAwNQYIKwYBBQUHAgEWKWh0dHA6Ly9vYi50cnVzdGlzLmNvbS9wcm9kdWN0aW9uL3BvbGljaWVzMIH2BggrBgEFBQcCAjCB6QyB5lRoaXMgQ2VydGlmaWNhdGUgaXMgc29sZWx5IGZvciB1c2Ugd2l0aCBPcGVuIEJhbmtpbmcgTGltaXRlZCBhbmQgYXNzb2NpYXRlZCBPcGVuIEJhbmtpbmcgU2VydmljZXMuIEl0cyByZWNlaXB0LCBwb3NzZXNzaW9uIG9yIHVzZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBPcGVuIEJhbmtpbmcgTGltaXRlZCBDZXJ0aWZpY2F0ZSBQb2xpY3kgYW5kIHJlbGF0ZWQgZG9jdW1lbnRzIHRoZXJlaW4uMHIGCCsGAQUFBwEBBGYwZDAmBggrBgEFBQcwAYYaaHR0cDovL29iLnRydXN0aXMuY29tL29jc3AwOgYIKwYBBQUHMAKGLmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9wcm9kdWN0aW9uL2lzc3VpbmdjYS5jcnQwPwYDVR0fBDgwNjA0oDKgMIYuaHR0cDovL29iLnRydXN0aXMuY29tL3Byb2R1Y3Rpb24vaXNzdWluZ2NhLmNybDAfBgNVHSMEGDAWgBSfSb9ONqesww8ryEf0HykbwHkLBTAdBgNVHQ4EFgQUrkbRhKkGfBoKQwGiZY8sNm8fLr0wDQYJKoZIhvcNAQELBQADggEBAAzL5ixmM4lUlsqz1dShjdwFZtMvSFje+bvOec66z3TY3X7MdJ4MR3InFj0dvo69x0s3j8XziIoiXLJUuvnRXgUs5XuettpkVH19ZatA9dDIhOQxfQfJLBA8GcGem/xS809Xp5cRNuNhkLkdFhGa2MzzjWb2PTJBKQRoA3VJ/HKCEYnkPIYKCQ4zyTwaH0vQI/F5y1+0alWsNwGHhIS33H8ribGXv47A+eSJ80BAXZV9XK1Ypqy/QvjSIZvPCGWXTnI/hLbL6zv4xIWVILPnPPCynuyEmFWbQD7jZvk2nqwlb/gectxhA3x4VMnUufewAgHJWGhhgdMXCXGGOyLQ7/p0s4NA9EkNdn0DR8XsSc9BupOvHg==");
    private static final String UKOB_ROOT_CA_JKS_PASSWORD = "tinktink";

    private final URL wellKnownURL;
    private final JwtSigner jwtSigner;

    protected UkOpenBankingApiClient apiClient;
    protected SoftwareStatementAssertion softwareStatement;
    protected ProviderConfiguration providerConfiguration;
    private boolean disableSslVerification;

    private TransferDestinationRefreshController transferDestinationRefreshController;
    private CreditCardRefreshController creditCardRefreshController;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    // Lazy loaded
    private UkOpenBankingAis aisSupport;
    private final UkOpenBankingAisConfig agentConfig;
    private AccountFetcher<TransactionalAccount> transactionalAccountFetcher;

    protected final RandomValueGenerator randomValueGenerator;
    protected final LocalDateTimeSource localDateTimeSource;

    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig agentConfig,
            boolean disableSslVerification) {
        super(componentProvider);
        this.wellKnownURL = agentConfig.getWellKnownURL();
        this.jwtSigner = jwtSigner;
        this.disableSslVerification = disableSslVerification;
        this.agentConfig = agentConfig;
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();

        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ReAuthenticateFilter(persistentStorage));
    }

    // Different part between UkOpenBankingBaseAgent and this class
    public AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentCommonConfiguration(getClientConfigurationFormat());
    }

    @Override
    public final void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration =
                getAgentConfiguration().getClientConfiguration();

        softwareStatement = ukOpenBankingConfiguration.getSoftwareStatementAssertion();

        providerConfiguration = ukOpenBankingConfiguration.getProviderConfiguration();

        if (this.disableSslVerification) {
            client.disableSslVerification();
        } else {
            client.trustRootCaCertificate(UKOB_ROOT_CA_JKS, UKOB_ROOT_CA_JKS_PASSWORD);
        }

        ukOpenBankingConfiguration
                .getTlsConfigurationOverride()
                .orElse(this::useEidasProxy)
                .applyConfiguration(client);

        final String redirectUrl = getAgentConfiguration().getRedirectUrl();

        apiClient =
                createApiClient(
                        client, jwtSigner, softwareStatement, redirectUrl, providerConfiguration);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private void useEidasProxy(TinkHttpClient httpClient) {
        httpClient.setEidasProxy(configuration.getEidasProxy());
    }

    protected static JwtSigner createEidasJwtSigner(
            final AgentsServiceConfiguration configuration,
            final EidasContext context,
            final Class<? extends UkOpenBankingBaseAgent> agentClass) {
        final EidasIdentity identity =
                new EidasIdentity(context.getClusterId(), context.getAppId(), agentClass);
        return new EidasJwtSigner(configuration.getEidasProxy().toInternalConfig(), identity);
    }

    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ProviderConfiguration providerConfiguration) {
        return new UkOpenBankingApiClient(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                wellKnownURL,
                randomValueGenerator,
                persistentStorage,
                agentConfig);
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    protected Authenticator constructAuthenticator(UkOpenBankingAisConfig aisConfig) {
        UkOpenBankingAisAuthenticator authenticator = new UkOpenBankingAisAuthenticator(apiClient);
        return createOpenIdFlowWithAuthenticator(authenticator, aisConfig.getAppToAppURL());
    }

    protected final Authenticator createOpenIdFlowWithAuthenticator(
            UkOpenBankingAisAuthenticator authenticator, URL appToAppRedirectURL) {
        return OpenIdAuthenticationFlow.create(
                request,
                context,
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                apiClient,
                credentials,
                strongAuthenticationState,
                appToAppRedirectURL,
                randomValueGenerator);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        UkOpenBankingAis ais = getAisSupport();

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                getTransactionalAccountFetcher(),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        ais.makeAccountTransactionPaginatorController(apiClient),
                        ais.makeUpcomingTransactionFetcher(apiClient).orElse(null)));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        UkOpenBankingAis ais = getAisSupport();

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                ais.makeCreditCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        ais.makeCreditCardTransactionPaginatorController(apiClient)));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    protected TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new UkOpenBankingTransferDestinationFetcher(
                        apiClient, AccountIdentifier.Type.SORT_CODE, SortCodeIdentifier.class));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return getAisSupport()
                .makeIdentityDataFetcher(apiClient)
                .fetchIdentityData()
                .map(FetchIdentityDataResponse::new)
                .orElse(
                        new FetchIdentityDataResponse(
                                IdentityData.builder()
                                        .setFullName(null)
                                        .setDateOfBirth(null)
                                        .build()));
    }

    private AccountFetcher<TransactionalAccount> getTransactionalAccountFetcher() {
        if (Objects.nonNull(transactionalAccountFetcher)) {
            return transactionalAccountFetcher;
        }

        transactionalAccountFetcher = getAisSupport().makeTransactionalAccountFetcher(apiClient);
        return transactionalAccountFetcher;
    }

    private UkOpenBankingAis getAisSupport() {
        if (Objects.nonNull(aisSupport)) {
            return aisSupport;
        }
        aisSupport = makeAis();
        return aisSupport;
    }

    protected abstract UkOpenBankingAis makeAis();

    protected Class<? extends UkOpenBankingClientConfigurationAdapter>
            getClientConfigurationFormat() {
        return UkOpenBankingConfiguration.class;
    }
}
