package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "initialization")
@XmlAccessorType(XmlAccessType.FIELD)
public class InitializationEntity {

    @XmlElement(name = "user_infos")
    private String userInfos;

    @XmlElement(name = "has_aggregation")
    private String hasAggregation;

    @XmlElement(name = "version")
    private String version;

    @XmlElement(name = "public_path")
    private String publicPath;

    @XmlElement(name = "user_rules")
    private String userRules;

    @XmlElement(name = "user_parameters")
    private String userParameters;

    @XmlElement(name = "has_updates")
    private String hasUpdates;

    public String getUserInfos() {
        return userInfos;
    }

    public String getHasAggregation() {
        return hasAggregation;
    }
}
