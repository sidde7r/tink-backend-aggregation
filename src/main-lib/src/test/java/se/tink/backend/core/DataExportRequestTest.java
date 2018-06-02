package se.tink.backend.core;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DataExportRequestTest {

    DataExportRequest dataExportRequest;

    @Before
    public void setUp() {
        dataExportRequest = new DataExportRequest();
    }

    @Test
    public void testIdGeneratedOnCreationDataExportRequest() {
        assertThat(dataExportRequest.getId()).isNotNull();
    }

    @Test
    public void testDateGeneratedOnCreationDateExportRequest() {
        assertThat(dataExportRequest.getCreated()).isNotNull();
    }

    @Test
    public void testNullOrEmptyUserIdDataExportRequest() {
        assertThatThrownBy(() -> dataExportRequest.setUserId(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> dataExportRequest.setUserId("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNullOrEmptyIdDataExportRequest() {
        assertThatThrownBy(() -> dataExportRequest.setId(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> dataExportRequest.setId("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSetNullOnNotNullFieldsDataExportRequest() {
        assertThatThrownBy(() -> dataExportRequest.setCreated(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> dataExportRequest.setStatus(null)).isInstanceOf(NullPointerException.class);
    }
}
