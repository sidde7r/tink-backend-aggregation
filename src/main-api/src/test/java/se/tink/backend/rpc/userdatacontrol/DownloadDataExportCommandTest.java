package se.tink.backend.rpc.userdatacontrol;

import org.junit.Test;
import se.tink.backend.rpc.DownloadDataExportCommand;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class DownloadDataExportCommandTest {
    @Test
    public void testCorrectConstruction() {
        DownloadDataExportCommand command = new DownloadDataExportCommand("4723b2b2-0e15-4550-af7b-fdeea86acc63",
                "1716d661-ee4e-4f07-8992-f4e1bf313c6f");

        assertThat(command.getUserId()).isEqualTo(UUIDUtils.fromString("4723b2b2-0e15-4550-af7b-fdeea86acc63"));
        assertThat(command.getId()).isEqualTo(UUIDUtils.fromString("1716d661-ee4e-4f07-8992-f4e1bf313c6f"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserIdInput() {
        new DownloadDataExportCommand(null, "1716d661-ee4e-4f07-8992-f4e1bf313c6f");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUserIdInput() {
        new DownloadDataExportCommand("", "1716d661-ee4e-4f07-8992-f4e1bf313c6f");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullIdInput() {
        new DownloadDataExportCommand("4723b2b2-0e15-4550-af7b-fdeea86acc63", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyIdInput() {
        new DownloadDataExportCommand("4723b2b2-0e15-4550-af7b-fdeea86acc63", "");
    }
}
