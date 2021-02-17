package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.BaseResponsePart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.TanByOperationLookup;

@Ignore
public class TestFixtures {

    private TestFixtures() {}

    static String getBodyOfSuccessfulAccountsDetailsResponse() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDUzOSszMDArMDAwMDA2R0lEVE1DT0lVTjBTUzJIUTlFOTJQMUY3"
                + "KzIrMDAwMDA2R0lEVE1DT0lVTjBTUzJIUTlFOTJQMUY3OjInSE5WU0s6OTk4OjMrUElOOjIrOTk4"
                + "KzErMjo6MDAwMDA2R0lEVExQRlNGQk4xTkRTMUFCT0ExQUgwKzErMjoyOjEzOkA4QAAAAAAAAAAA"
                + "OjY6MSsyODA6NTAwMTA1MTc6NTQyNTcyODE2MTpWOjA6MCswJ0hOVlNEOjk5OToxK0AzMDFASElS"
                + "TUc6MjoyOiswMDEwOjpEaWUgTmFjaHJpY2h0IHd1cmRlIGVudGdlZ2VuZ2Vub21tZW4uJ0hJUk1T"
                + "OjM6MjozKzAwMjA6OkRlciBBdWZ0cmFnIHd1cmRlIGF1c2dlZvxocnQuJ0hJU1BBOjQ6MTozK0o6"
                + "REU4NzUwMDEwNTE3NTQyNTcyODE2MTpJTkdEREVGRlhYWDo1NDI1NzI4MTYxOjoyODA6NTAwMTA1"
                + "MTcrSjpERTUwNTAwMTA1MTc1NTY3NDMwNjk1OklOR0RERUZGWFhYOjU1Njc0MzA2OTU6OjI4MDo1"
                + "MDAxMDUxNytKOkRFNTc1MDAxMDUxNzgwMTA4MjUwNjc6SU5HRERFRkZYWFg6ODAxMDgyNTA2Nzo6"
                + "MjgwOjUwMDEwNTE3JydITkhCUzo1OjErMic=";
    }

    static String getBodyOfSuccessfulAccountBalanceResponse() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDQxMCszMDArMzc2MjQxNDQwNjI0NzAwMDU5TCpYcU9xZjIuRDExKz"
                + "QrMzc2MjQxNDQwNjI0NzAwMDU5TCpYcU9xZjIuRDExOjQnSE5WU0s6OTk4OjMrUElOOjErOTk4KzE"
                + "rMjo6Mzc2MjQxNDQwNTk3OTAwMEpITUhIQzVRM1U5MVZKKzE6MjAyMDAzMjM6MTIwNjU1KzI6Mjox"
                + "MzpAOEAAAAAAAAAAADo1OjErMjgwOjEwMDEwMDEwOmphbmdpbGxpY2g6VjowOjArMCdITlZTRDo5O"
                + "Tk6MStAMTU2QEhJUk1HOjI6MiswMDEwOjpOYWNocmljaHQgZW50Z2VnZW5nZW5vbW1lbi4nSElSTV"
                + "M6MzoyOjMrMDAyMDo6QXVmdHJhZyBhdXNnZWb8aHJ0LidISVNBTDo0OjY6Mys1NTQ0ODExMjc6OjI"
                + "4MDoxMDAxMDAxMCtQQiBHaXJvIERpcmVrdCtFVVIrQzoyLDU6RVVSOjIwMjAwMzIzJydITkhCUzo1"
                + "OjErNCc=";
    }

    static String getBodyOfUnsuccessfulResponse() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDMzMyszMDArMzc2MjQxNDQwNjI0NzAwMDU5TCpYcU9xZjIuRDExKzU"
                + "rMzc2MjQxNDQwNjI0NzAwMDU5TCpYcU9xZjIuRDExOjUnSE5WU0s6OTk4OjMrUElOOjErOTk4KzErM"
                + "jo6Mzc2MjQxNDQwNTk3OTAwMEpITUhIQzVRM1U5MVZKKzE6MjAyMDAzMjM6MTIwNjU1KzI6MjoxMzp"
                + "AOEAAAAAAAAAAADo1OjErMjgwOjEwMDEwMDEwOmphbmdpbGxpY2g6VjowOjArMCdITlZTRDo5OTk6M"
                + "StAODBASElSTUc6MjoyKzkwNTA6OlRlaWx3ZWlzZSBmZWhsZXJoYWZ0LidISVJNUzozOjI6Mys5MjE"
                + "wOjpXaXJkIG5pY2h0IHVudGVyc3T8dHp0LicnSE5IQlM6NDoxKzUn";
    }

    static FinTsConfiguration getFinTsConfiguration(final int port) {
        final String socket = String.format("http://localhost:%d/foo/bar", port);
        return new FinTsConfiguration("foo", Bank.POSTBANK, socket, "foo", "foo");
    }

    static FinTsDialogContext getDialogContext(FinTsConfiguration configuration) {
        FinTsDialogContext context =
                new FinTsDialogContext(configuration, new FinTsSecretsConfiguration(null, null));
        BaseResponsePart part = mock(BaseResponsePart.class);
        when(part.getSegmentVersion()).thenReturn(5);
        context.addOperationSupportedByBank(SegmentType.HKSAL, part);
        context.setTanByOperationLookup(getOperationLookup());
        return context;
    }

    private static TanByOperationLookup getOperationLookup() {
        HIPINS hipins =
                new HIPINS()
                        .setOperations(
                                Arrays.asList(
                                        Pair.of(SegmentType.HKSPA.getSegmentName(), false),
                                        Pair.of(SegmentType.HKSAL.getSegmentName(), false)));
        return new TanByOperationLookup(hipins);
    }
}
