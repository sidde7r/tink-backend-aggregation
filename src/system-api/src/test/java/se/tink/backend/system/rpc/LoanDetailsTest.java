package se.tink.backend.system.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class LoanDetailsTest {

    @Test
    public void mapRpcModelJsonToCore() throws IOException {
        se.tink.backend.system.rpc.LoanDetails loanDetailsDto = new se.tink.backend.system.rpc.LoanDetails();
        List<String> applicants = asList("applicant1", "applicant2");
        loanDetailsDto.setApplicants(applicants);

        ObjectMapper jsonMapper = new ObjectMapper();
        assertEquals(applicants,
                jsonMapper.readValue(jsonMapper.writeValueAsString(loanDetailsDto),
                        se.tink.backend.core.LoanDetails.class).getApplicants());
    }
}
