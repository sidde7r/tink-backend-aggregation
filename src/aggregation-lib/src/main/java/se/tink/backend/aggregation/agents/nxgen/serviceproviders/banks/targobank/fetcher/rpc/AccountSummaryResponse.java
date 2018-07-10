package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankMsgDateDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities.CategoryEntities;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountSummaryResponse {
    @XmlElement(name = "code_retour")
    private String returnCode;

    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(TargoBankMsgDateDeserializer.class)
    private Date date;

    @XmlElement(name = "category_list")
    private CategoryEntities categoryList;
    @XmlElementWrapper(name = "liste_compte")
    @XmlElement(name = "compte")
    private List<AccountDetailsEntity> accountDetailsList;

    public CategoryEntities getCategoryList() {
        return categoryList;
    }

    public List<AccountDetailsEntity> getAccountDetailsList() {
        return accountDetailsList;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public Date getDate() {
        return date;
    }
}
