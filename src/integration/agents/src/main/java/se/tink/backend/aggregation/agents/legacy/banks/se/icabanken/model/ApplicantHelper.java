package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.List;

public class ApplicantHelper {
    private static final Splitter SPLITTER = Splitter.on("och").trimResults();
    private boolean coApplicants;
    private List<String> applicants;

    public ApplicantHelper(String applicants) {
        List<String> applicantsAsList = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(applicants)) {
            if (applicants.contains("och")) {
                Iterable<String> multipleApplicants = SPLITTER.split(applicants);
                for (String applicant : multipleApplicants) {
                    applicantsAsList.add(applicant);
                }
                this.coApplicants = true;
                this.applicants = applicantsAsList;
            } else {
                applicantsAsList.add(applicants);
                this.coApplicants = false;
                this.applicants = applicantsAsList;
            }
        }
    }

    public boolean isCoApplicants() {
        return coApplicants;
    }

    public void setCoApplicants(boolean coApplicants) {
        this.coApplicants = coApplicants;
    }

    public List<String> getApplicants() {
        return applicants;
    }

    public void setApplicants(List<String> applicants) {
        this.applicants = applicants;
    }
}
