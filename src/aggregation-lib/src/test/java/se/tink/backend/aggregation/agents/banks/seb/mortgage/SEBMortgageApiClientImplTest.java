package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.PropertyType;
import se.tink.backend.aggregation.agents.utils.CreateProductExecutorTracker;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.ProductType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class SEBMortgageApiClientImplTest {
    public static class Api {
        private HttpClient mockClient;
        private SEBMortgageApiClientImpl sebMortgageApiClient;

        @Before
        public void setup() {
            mockClient = mock(HttpClient.class);
            sebMortgageApiClient = new SEBMortgageApiClientImpl(mockClient, mock(CreateProductExecutorTracker.class));
        }

        @Test
        public void createMortgageCase() throws Exception {
            LoanPostRequest requestStub = new LoanPostRequest();
            LoanPostResponse responseStub = new LoanPostResponse();

            // Setup client mock
            when(mockClient
                    .post(requestStub, LoanPostResponse.class))
                    .thenReturn(responseStub);

            // Test return value
            LoanPostResponse returnedResponse = sebMortgageApiClient.createMortgageCase(requestStub);

            assertThat(returnedResponse).isEqualTo(responseStub);
        }

        @Test
        public void getMortgageStatus() throws Exception {
            GetLoanStatusRequest requestStub = new GetLoanStatusRequest("application id");
            GetLoanStatusResponse responseStub = new GetLoanStatusResponse();

            // Setup client mock
            when(mockClient
                    .get(requestStub, GetLoanStatusResponse.class))
                    .thenReturn(responseStub);

            // Test return value
            GetLoanStatusResponse mortgageStatus = sebMortgageApiClient.getMortgageStatus(requestStub);

            assertThat(mortgageStatus).isEqualTo(responseStub);
        }

        @Test
        public void getMortgageStatusSign() throws Exception {
            GetLoanStatusSignRequest requestStub = new GetLoanStatusSignRequest("application id");
            GetLoanStatusSignResponse responseStub = new GetLoanStatusSignResponse();

            // Setup client mock
            when(mockClient
                    .get(requestStub, GetLoanStatusSignResponse.class))
                    .thenReturn(responseStub);

            // Test return value
            GetLoanStatusSignResponse mortgageStatus = sebMortgageApiClient.getMortgageStatusSign(requestStub);

            assertThat(mortgageStatus).isEqualTo(responseStub);
        }

        @Test
        public void getMortgageStatusSign_when403Response_returnsValidationError() throws Exception {
            GetLoanStatusSignRequest requestStub = new GetLoanStatusSignRequest("application id");
            GetLoanStatusSignResponse responseStub = new GetLoanStatusSignResponse();
            responseStub.setStatus(GetLoanStatusSignResponse.BankIdStatus.USER_VALIDATION_ERROR);

            // As soon as we get a 403 we expect the user validation error to be returned gracefully from the client
            ClientResponse clientResponseMock = mock(ClientResponse.class);
            when(clientResponseMock.getStatus()).thenReturn(403);
            UniformInterfaceException forbiddenResponse = mock(UniformInterfaceException.class);
            when(forbiddenResponse.getResponse()).thenReturn(clientResponseMock);

            // Setup client mock
            when(mockClient
                    .get(requestStub, GetLoanStatusSignResponse.class))
                    .thenThrow(forbiddenResponse);

            // Test return value
            GetLoanStatusSignResponse mortgageStatus = sebMortgageApiClient.getMortgageStatusSign(requestStub);

            assertThat(mortgageStatus).isEqualTo(responseStub);
        }

        @Test
        public void getRate() throws Exception {
            GetRateRequest requestStub = GetRateRequest.builder()
                    .withAge(10).withLoanAmount(123.00).withPropertyType(PropertyType.VILLA)
                    .build();
            GetRateResponse responseStub = new GetRateResponse();

            // Setup client mock
            when(mockClient
                    .get(requestStub, GetRateResponse.class))
                    .thenReturn(responseStub);

            // Test return value
            GetRateResponse mortgageStatus = sebMortgageApiClient.getRate(requestStub);

            assertThat(mortgageStatus).isEqualTo(responseStub);
        }

        @Test
        public void getRate_when503Response_trackStatus() {

            CreateProductExecutorTracker tracker = getTrackerMock();
            GetRateRequest request = getRequest();

            // Mock HTTP 503 response.
            ClientResponse response = mock(ClientResponse.class);
            when(response.getStatus()).thenReturn(503);

            UniformInterfaceException exception = mock(UniformInterfaceException.class);
            when(exception.getResponse()).thenReturn(response);

            // Mock client which throws above exception.
            SEBMortgageApiClient sebMortgageApiClient = getFailingClient(exception, tracker);
            try {
                sebMortgageApiClient.getRate(request);
            } catch (Exception e) {
                // Do nothing.
            }

            verify(tracker, times(1)).trackFetchProductInformation("seb", ProductType.MORTGAGE, 503);
        }

        @Test
        public void getRate_whenException_doNotTrackStatus() {

            CreateProductExecutorTracker tracker = getTrackerMock();
            GetRateRequest request = getRequest();

            // Create internal exception.
            Exception exception = new ClientHandlerException("Problems building the request.");

            // Mock client which throws above exception.
            SEBMortgageApiClient sebMortgageApiClient = getFailingClient(exception, tracker);
            try {
                sebMortgageApiClient.getRate(request);
            } catch (Exception e) {
                // Do nothing.
            }

            verify(tracker, never()).trackFetchProductInformation(eq("seb"), eq(ProductType.MORTGAGE), anyInt());
        }

        private CreateProductExecutorTracker getTrackerMock() {
            return mock(CreateProductExecutorTracker.class);
        }

        private SEBMortgageApiClient getFailingClient(Exception exception, CreateProductExecutorTracker tracker) {

            HttpClient client = mock(HttpClient.class);
            when(client.get(any(), any())).thenThrow(exception);

            return new SEBMortgageApiClientImpl(client, tracker);
        }

        private GetRateRequest getRequest() {
            return GetRateRequest.builder().withAge(30).withLoanAmount(2000000d)
                    .withPropertyType(PropertyType.APARTMENT).build();
        }
    }

    public static class HttpFilter {
        @Test
        public void addHttpFilter_addsFilterOnClient() {
            ClientFilterFactory filterMock = mock(ClientFilterFactory.class);
            HttpClient clientMock = mock(HttpClient.class);
            CreateProductExecutorTracker trackerMock = mock(CreateProductExecutorTracker.class);
            SEBMortgageApiClient apiClient = new SEBMortgageApiClientImpl(clientMock, trackerMock);

            apiClient.attachHttpFilters(filterMock);

            verify(clientMock, times(1)).attachHttpFilters(filterMock);
        }
    }
}
