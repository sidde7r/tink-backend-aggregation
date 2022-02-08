package se.tink.backend.aggregation.queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        given(localDateTimeSource.nowZonedDateTime(any()))
                .willReturn(ZonedDateTime.of(2021, 1, 15, 8, 0, 0, 0, ZoneOffset.UTC));
        automaticRefreshValidator = new AutomaticRefreshValidator(localDateTimeSource);
    }

    @Test
    public void shouldNotThrowExceptionWhenExpiryDateIsAfterNow() {
        // given
        ZonedDateTime now = ZonedDateTime.of(2021, 1, 15, 8, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime expiryDate =
                ZonedDateTime.of(2021, 1, 15, 7, 1, 0, 0, ZoneOffset.ofHours(-2));

        int a = now.compareTo(expiryDate);

        Provider provider = new Provider();
        provider.setName("Provider");
        RefreshInformationRequest request =
                RefreshInformationRequest.builder()
                        .expiryDate(ZonedDateTime.of(2021, 1, 15, 8, 1, 0, 0, ZoneOffset.UTC))
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
                        .expiryDate(ZonedDateTime.of(2021, 1, 15, 7, 59, 0, 0, ZoneOffset.UTC))
                        .provider(provider)
                        .build();

        // when
        automaticRefreshValidator.validate(request);
    }

    @Test(expected = ExpiredMessageException.class)
    public void shouldThrowExceptionWhenExpiryDateIsBeforeNowInDifferentZoneId() {
        // given
        Provider provider = new Provider();
        provider.setName("Provider");
        RefreshInformationRequest request =
                RefreshInformationRequest.builder()
                        .expiryDate(
                                ZonedDateTime.of(2021, 1, 15, 8, 59, 0, 0, ZoneOffset.ofHours(3)))
                        .provider(provider)
                        .build();

        // when
        automaticRefreshValidator.validate(request);
    }
}
