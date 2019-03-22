package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Context")
@XmlType(propOrder = {"channel", "language", "devID", "deviceIdentifier"})
public class Context {
    private String Channel;
    private String Language;
    private String DevID;
    private String DeviceIdentifier;

    public String getChannel() {
        return Channel;
    }

    public String getLanguage() {
        return Language;
    }

    public String getDevID() {
        return DevID;
    }

    public String getDeviceIdentifier() {
        return DeviceIdentifier;
    }

    @XmlElement(name = "Channel")
    public void setChannel(String Channel) {
        this.Channel = Channel;
    }

    @XmlElement(name = "Language")
    public void setLanguage(String Language) {
        this.Language = Language;
    }

    @XmlElement(name = "DevID")
    public void setDevID(String DevID) {
        this.DevID = DevID;
    }

    @XmlElement(name = "DeviceIdentifier")
    public void setDeviceIdentifier(String DeviceIdentifier) {
        this.DeviceIdentifier = DeviceIdentifier;
    }
}
