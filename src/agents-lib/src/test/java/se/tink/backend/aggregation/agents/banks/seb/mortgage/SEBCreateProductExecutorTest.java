package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping.ApplicationToLoanPostRequestMapper;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping.ApplicationToLoanPostRequestMapperImpl;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.MortgageStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.ApplicationState;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.aggregation.rpc.ProductType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class SEBCreateProductExecutorTest {
    public static class CreateProduct {
        @Test
        public void createProduct() throws BankIdException, InvalidApplicationException {
            AgentContext contextMock = mock(AgentContext.class);
            Credentials credentialsMock = mock(Credentials.class);
            AggregationLogger logMock = mock(AggregationLogger.class);
            SEBMortgageApiClient clientMock = mock(SEBMortgageApiClient.class);
            SEBMortgageBankIdCollector bankIdCollectorMock = mock(SEBMortgageBankIdCollector.class);
            ApplicationToLoanPostRequestMapper applicationMapperMock = mock(ApplicationToLoanPostRequestMapperImpl.class);
            SEBCreateProductExecutor executor = new SEBCreateProductExecutor(
                    contextMock,
                    credentialsMock,
                    logMock,
                    clientMock,
                    bankIdCollectorMock,
                    applicationMapperMock,
                    mock(ProductInformationGetRatesMapper.class));

            // Stub an application
            UUID applicationId = UUID.randomUUID();
            GenericApplication application = new GenericApplication();
            application.setApplicationId(applicationId);

            // Stub a mapped request to be returned when application sent in
            LoanPostRequest loanPostRequestStub = new LoanPostRequest();
            when(applicationMapperMock.toLoanRequest(application)).thenReturn(loanPostRequestStub);

            // Stubbed response from client
            LoanPostResponse loanResponse = new LoanPostResponse();
            loanResponse.setApplicationId("external-id-1234");
            when(clientMock.createMortgageCase(loanPostRequestStub)).thenReturn(loanResponse);

            // Stubbed bankid response
            when(bankIdCollectorMock.collect("external-id-1234")).thenReturn(
                    GetLoanStatusSignResponse.BankIdStatus.COMPLETE);

            // Create the application
            assertThat(executor.create(application).getExternalId()).isEqualTo("external-id-1234");

            // Verify the end goal - That the client received the loan request
            verify(clientMock, times(1)).createMortgageCase(loanPostRequestStub);
            verify(credentialsMock, times(1)).setSupplementalInformation(null);
            verify(credentialsMock, times(1)).setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
            verify(contextMock, times(1)).requestSupplementalInformation(credentialsMock, false);
            verify(bankIdCollectorMock, times(1)).collect("external-id-1234");

            verifyNoMoreInteractions(contextMock);
        }
    }

    public static class FetchProductInformation {
        @Test(expected = UnsupportedOperationException.class)
        public void notImplementedProduct_willThrow() {
            SEBCreateProductExecutor executor = new SEBCreateProductExecutor(
                    mock(AgentContext.class),
                    mock(Credentials.class),
                    mock(AggregationLogger.class),
                    mock(SEBMortgageApiClient.class),
                    mock(SEBMortgageBankIdCollector.class),
                    mock(ApplicationToLoanPostRequestMapper.class),
                    mock(ProductInformationGetRatesMapper.class));

            executor.fetchProductInformation(
                    ProductType.SAVINGS_ACCOUNT,
                    UUID.randomUUID(),
                    Maps.<FetchProductInformationParameterKey, Object>newHashMap());
        }

        @Test
        public void whenRequestingRate_ensureExpectedBehavior() {
            // Setup instances
            AgentContext contextMock = mock(AgentContext.class);
            SEBMortgageApiClient clientMock = mock(SEBMortgageApiClient.class);
            ProductInformationGetRatesMapper mapperMock = mock(ProductInformationGetRatesMapper.class);

            SEBCreateProductExecutor executor = new SEBCreateProductExecutor(
                    contextMock,
                    mock(Credentials.class),
                    mock(AggregationLogger.class),
                    clientMock,
                    mock(SEBMortgageBankIdCollector.class),
                    mock(ApplicationToLoanPostRequestMapper.class),
                    mapperMock);

            Map<FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();

            // Mapper maps certain params to request
            GetRateRequest request = mock(GetRateRequest.class);
            when(mapperMock.toRateRequest(parameters)).thenReturn(request);
            // Client return for specific request
            GetRateResponse response = mock(GetRateResponse.class);
            when(clientMock.getRate(request)).thenReturn(response);
            // Mapper maps to certain output for given response
            HashMap<ProductPropertyKey, Object> productProperties = new HashMap<>();
            when(mapperMock.toProductProperties(response)).thenReturn(productProperties);

            // Run
            UUID productInstanceId = UUID.randomUUID();
            executor.fetchProductInformation(
                    ProductType.MORTGAGE,
                    productInstanceId,
                    parameters);

            // Verify expected interactions
            verify(mapperMock, times(1)).toRateRequest(parameters);
            verify(clientMock, times(1)).getRate(request);
            verify(mapperMock, times(1)).toProductProperties(response);
            verify(contextMock, times(1)).updateProductInformation(productInstanceId, productProperties);
            verifyNoMoreInteractions(mapperMock, clientMock, contextMock);
        }
    }

    public static class RefreshApplication {
        @Test(expected = IllegalArgumentException.class)
        public void noExternalId_willThrow() {
            SEBCreateProductExecutor executor = new SEBCreateProductExecutor(
                    mock(AgentContext.class),
                    mock(Credentials.class),
                    mock(AggregationLogger.class),
                    mock(SEBMortgageApiClient.class),
                    mock(SEBMortgageBankIdCollector.class),
                    mock(ApplicationToLoanPostRequestMapper.class),
                    mock(ProductInformationGetRatesMapper.class));

            executor.refreshApplication(ProductType.MORTGAGE, UUID.randomUUID(),
                    Maps.<RefreshApplicationParameterKey, Object>newHashMap());
        }

        @Test
        public void whenRefreshingApplication_ensureExpectedBehavior() {
            // Setup instances
            AgentContext contextMock = mock(AgentContext.class);
            SEBMortgageApiClient clientMock = mock(SEBMortgageApiClient.class);

            SEBCreateProductExecutor executor = new SEBCreateProductExecutor(
                    contextMock,
                    mock(Credentials.class),
                    mock(AggregationLogger.class),
                    clientMock,
                    mock(SEBMortgageBankIdCollector.class),
                    mock(ApplicationToLoanPostRequestMapper.class),
                    mock(ProductInformationGetRatesMapper.class));

            Map<RefreshApplicationParameterKey, Object> parameters = ImmutableMap.of(
                    RefreshApplicationParameterKey.EXTERNAL_ID, (Object) "abc123");

            // Client return for specific request
            GetLoanStatusRequest request = new GetLoanStatusRequest("abc123");
            GetLoanStatusResponse response = new GetLoanStatusResponse();
            response.setStatus(MortgageStatus.APPLICATION_APPROVED);
            response.setDescription("desc");
            when(clientMock.getMortgageStatus(eq(request))).thenReturn(response);

            // Run
            UUID applicationId = UUID.randomUUID();
            executor.refreshApplication(
                    ProductType.MORTGAGE,
                    applicationId,
                    parameters);

            // Verify expected interactions
            ArgumentCaptor<ApplicationState> applicationStateCaptor = ArgumentCaptor.forClass(ApplicationState.class);
            verify(clientMock, times(1)).getMortgageStatus(request);
            verify(contextMock, times(1)).updateApplication(eq(applicationId), applicationStateCaptor.capture());
            verifyNoMoreInteractions(clientMock, contextMock);

            ApplicationState applicationState = applicationStateCaptor.getValue();
            assertThat(applicationState.getNewApplicationStatus().isPresent()).isTrue();
            assertThat(applicationState.getNewApplicationStatus().get()).isEqualTo(ApplicationStatusKey.APPROVED);
            assertThat(applicationState.getApplicationProperties())
                    .containsEntry(ApplicationPropertyKey.EXTERNAL_STATUS,
                            MortgageStatus.APPLICATION_APPROVED)
                    .containsEntry(ApplicationPropertyKey.EXTERNAL_STATUS_DESCRIPTION, "desc");
        }
    }

    public static class HttpFilter {
        @Test
        public void addHttpFilter_addsFilterOnClient() {
            ClientFilterFactory filterFactoryMock = mock(ClientFilterFactory.class);
            SEBMortgageApiClient clientMock = mock(SEBMortgageApiClient.class);
            SEBCreateProductExecutor sebCreateProductExecutor = new SEBCreateProductExecutor(
                    mock(AgentContext.class),
                    mock(Credentials.class),
                    mock(AggregationLogger.class),
                    clientMock,
                    mock(SEBMortgageBankIdCollector.class),
                    mock(ApplicationToLoanPostRequestMapperImpl.class),
                    mock(ProductInformationGetRatesMapper.class));

            sebCreateProductExecutor.attachHttpFilters(filterFactoryMock);

            verify(clientMock, times(1)).attachHttpFilters(filterFactoryMock);
        }
    }
}
