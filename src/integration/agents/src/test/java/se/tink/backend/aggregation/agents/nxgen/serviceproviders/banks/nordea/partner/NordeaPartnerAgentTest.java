package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.AgentTestServerClient;

@Ignore
public class NordeaPartnerAgentTest {

    @Test
    public void testRefresh() throws Exception {
        String credentialsId = UUID.randomUUID().toString().replaceAll("-", "");
        AgentTestServerClient.getInstance()
                .initiateSupplementalInformation(
                        credentialsId,
                        "[{\"name\":\"token\", "
                                + "\"value\":\""
                                + "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTI4R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.dGlxQUQPlXSJfI5iyiofarRiaj6wKMrAKj3hrt1Q1xSO9nzMZjSsgfQzgBjUtDWSr0ix1kcs31T1u9g-GJkhbXO55teSN8zni2W2LukldW5Rp9LMy5sPECCRbowbPLXDBY5DSnguyMQlCXlTQy8Cq3U50O8kTaQJIng6ibUdj7xj-Fhi182kF7nf-lKZH7CAyJZE_QIy6c-vmeWDstq-Gg6jIzZsyLGfPIUAHuh-Vy2cR0sb-8BVFlQn7Z0ewliEXzsGkT5qWh-0tzX2vvUBzPW_8wDaHTx9GXCHqhTupGH64gT-6pNWwWh7457oBKG8QlyjqpuwV5VVqXPEHRE-_Q.LGqmLvYZ2dzgfY-y.h4rkF2DbPorIWB5ZtW32QmShQM7_aiisiU1M_ONW_ez4H5JTBkoo32t14UZGy1gsPrQlB16V1kC64v6RG29OatlPAM2EvRwEFKD4ghyW9arGBz9HBqM7wYJOHcFDT6mrtzr8qisy5DrpdiOjwazb1iPDt8HGbge5_h54MS8GHzDeb9oIQvmeLO43CgfroZ2g3Kxvk6jEjCy7cEzI-IHDq7sXUxM8U-pPW2RZmavjSWxfqubMWX-SNnCNI9Pe150K3R6Z24_FL8DRjj1KwR5pad7FqXlbHK8F75q9LorjdcEEG0tGeYP37s4X1rNgwvI-lMF_zwfrNzClSB5CQ6DQG30A-SQDGmD_WMUXikgAC8qR5EZECIgeLSNZr6840ISSI9rqlv8-JFmIunXfJSbse80B84AbKCs-53VRL9FedSUjiq8Vq1LSHuvmtW4VAKmY-tmYzCT6l9HfJQs0eBu3PHIuYxawJqYV8tuchJh-S3wCKRsla9UaCdLdV3spHp5IxR8f1dlYldDK8ismu-Kwgl9VfscO4-5Eu7H0DVfX4Ki4cxLu5xJBXHHqJuqb2VhAmrAkaBhWf_naw5_hf23t1ov-7SLjTNEAVLYK-bIgGMZA1-pKuTSYoP-8tWlFD6p9JQwuBjZi0HhJZmq2NqBv_-PmasGZ9khmfuDlmKmofjhlN5KBokKF5NSIldls5lnlBb0r1Xn39fOt7yFazkPAFd24b2srgPLieV6Ueqj3Z34r9HNb74ft9AWN20hnTg.c7i1n9lrFNXNDgxBZczlIQ"
                                + "\", "
                                + "\"description\":\"token\"}]");

        new AgentIntegrationTest.Builder("se", "se-nordeapartner-jwt")
                .setCredentialId(credentialsId)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setAppId("tink")
                .setFinancialInstitutionId("nordeapartner")
                .build()
                .testRefresh();
    }
}
