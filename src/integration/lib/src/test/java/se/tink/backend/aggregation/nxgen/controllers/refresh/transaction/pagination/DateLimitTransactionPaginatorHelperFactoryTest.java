package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;

@RunWith(JUnitParamsRunner.class)
public class DateLimitTransactionPaginatorHelperFactoryTest {

    @Mock private Date dateLimit;

    @Mock private CredentialsRequest request;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Parameters(method = "specifiedUserAvailableAndClassCreated")
    public void shouldCreateCorrectInstance(boolean isUserAvailable, Class<?> classReturned) {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(isUserAvailable);
        given(request.getUserAvailability()).willReturn(userAvailability);

        DateLimitTransactionPaginatorHelperFactory factory =
                new DateLimitTransactionPaginatorHelperFactory();
        // when
        TransactionPaginationHelper paginationHelper = factory.create(request, dateLimit);
        // then
        assertThat(paginationHelper).isInstanceOf(classReturned);
    }

    @SuppressWarnings("unused")
    private Object[] specifiedUserAvailableAndClassCreated() {
        return new Object[][] {
            {true, DateLimitTransactionPaginationHelper.class},
            {false, CertainDateTransactionPaginationHelper.class}
        };
    }
}
