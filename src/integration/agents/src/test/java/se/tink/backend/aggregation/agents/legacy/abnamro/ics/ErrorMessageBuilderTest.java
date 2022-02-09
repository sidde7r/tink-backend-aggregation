package se.tink.backend.aggregation.agents.abnamro.ics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.IcsException;
import se.tink.libraries.i18n_aggregation.Catalog;

public class ErrorMessageBuilderTest {

    @Test
    public void emtyBuilderShouldNotHaveExceptions() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        assertThat(builder.hasExceptions()).isFalse();
    }

    @Test
    public void testAddSingleException() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        builder.addException(11111L, new Exception());

        assertThat(builder.hasExceptions()).isTrue();

        assertThat(builder.build())
                .isEqualTo(
                        "A temporary error occurred in the communication between ABN and ICS for "
                                + "the credit card **** 1111. We will automatically refresh data as soon as the connection is "
                                + "available.");
    }

    @Test
    public void testAddMultipleExceptions() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        builder.addException(11111L, new Exception());
        builder.addException(22222L, new Exception());

        assertThat(builder.hasExceptions()).isTrue();

        assertThat(builder.build())
                .isEqualTo(
                        "A temporary error occurred in the communication between ABN and ICS for "
                                + "the credit cards **** 1111 and **** 2222. We will automatically refresh data as soon as the "
                                + "connection is available.");
    }

    @Test
    public void testAddSingleApprovalException() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        builder.addException(12345L, new IcsException("MESSAGE_BAI538_0002", "dummy"));

        assertThat(builder.hasExceptions()).isTrue();

        assertThat(builder.build())
                .isEqualTo(
                        "No transactions are available for credit card **** 2345. Please go to "
                                + "‘Settings’ > ‘Accounts’ in the Mobile Banking app to add this credit card. If your credit card "
                                + "transactions are displayed in Mobile Banking, they will also be available for Grip.");
    }

    @Test
    public void testAddExecutionExceptionWithSingleApprovalException() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        builder.addException(
                12345L, new ExecutionException(new IcsException("MESSAGE_BAI538_0002", "dummy")));

        assertThat(builder.hasExceptions()).isTrue();

        assertThat(builder.build())
                .isEqualTo(
                        "No transactions are available for credit card **** 2345. Please go to "
                                + "‘Settings’ > ‘Accounts’ in the Mobile Banking app to add this credit card. If your credit card "
                                + "transactions are displayed in Mobile Banking, they will also be available for Grip.");
    }

    @Test
    public void testAddMultipleApprovalException() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        builder.addException(12345L, new IcsException("MESSAGE_BAI538_0002", "dummy"));
        builder.addException(11111L, new IcsException("MESSAGE_BAI538_0002", "dummy"));

        assertThat(builder.hasExceptions()).isTrue();

        assertThat(builder.build())
                .isEqualTo(
                        "No transactions are available for credit cards **** 1111 and **** 2345. "
                                + "Please go to ‘Settings’ > ‘Accounts’ in the Mobile Banking app to add these credit cards. If your "
                                + "credit cards transactions are displayed in Mobile Banking, they will also be available for Grip.");
    }

    @Test
    public void testAddCombinedExceptions() {
        ErrorMessageBuilder builder = new ErrorMessageBuilder(Catalog.getCatalog("en_US"));

        builder.addException(12345L, new Exception());
        builder.addException(11111L, new IcsException("MESSAGE_BAI538_0002", "dummy"));

        assertThat(builder.hasExceptions()).isTrue();

        // Exceptions should be translated toa temporary error message
        String message = builder.build();

        assertThat(message.split("\n\n"))
                .contains(
                        "A temporary error occurred in the communication between ABN and ICS for"
                                + " the credit card **** 2345. We will automatically refresh data as soon as the connection "
                                + "is available.",
                        "No transactions are available for credit card **** 1111. Please go to ‘Settings’ > "
                                + "‘Accounts’ in the Mobile Banking app to add this credit card. If your credit card transactions are "
                                + "displayed in Mobile Banking, they will also be available for Grip.");
    }
}
