package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeDeserializer;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAddress;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelPersonalDataApproval;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class NickelUserResponse {

    private String accountUsage;
    private NickelAddress address;
    private String barcode;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private LocalDateTime birthDay;

    private String birthPlace;
    private String customerID;
    private String earnings;
    private String email;
    private Boolean emailEditable;
    private String firstName;
    private String formerProfession;
    private String formerProfessionLabel;
    private String formerProfessionPlus;
    private String housingStatus;
    private Boolean isFreelancer;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime lastConnectionDate;

    private String lastName;
    private String locale;
    private String maritalStatus;
    private String nationalityIso2;
    private NickelPersonalDataApproval personalDataApproval;
    private String phoneNumber;
    private Boolean phoneNumberEditable;
    private String profession;
    private String professionDetail;
}
