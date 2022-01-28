package se.tink.backend.aggregation.queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.queue.sqs.exception.ExpiredMessageException;

@RunWith(MockitoJUnitRunner.class)
public class AutomaticRefreshValidatorTest {

    private AutomaticRefreshValidator automaticRefreshValidator;

    @Mock private LocalDateTimeSource localDateTimeSource;

    @Before
    public void init() {
        given(localDateTimeSource.now(any())).willReturn(LocalDateTime.of(2021, 1, 15, 8, 0));
        automaticRefreshValidator = new AutomaticRefreshValidator(localDateTimeSource);
    }

    @Test
    public void shouldNotThrowExceptionWhenExpiryDateIsAfterNow() {
        // given
        Provider provider = new Provider();
        provider.setName("Provider");
        RefreshInformationRequest request =
                RefreshInformationRequest.builder()
                        .expiryDate(LocalDateTime.of(2021, 1, 15, 8, 1))
                        .provider(provider)
                        .build();

        // when
        automaticRefreshValidator.validate(request);
    }

    @Test(expected = ExpiredMessageException.class)
    public void shouldThrowExceptionWhenExpiryDateIsBeforeNow() {
        // given
        Provider provider = new Provider();
        provider.setName("Provider");
        RefreshInformationRequest request =
                RefreshInformationRequest.builder()
                        .expiryDate(LocalDateTime.of(2021, 1, 15, 7, 59))
                        .provider(provider)
                        .build();

        // when
        automaticRefreshValidator.validate(request);
    }
}
