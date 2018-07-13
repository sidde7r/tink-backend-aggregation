package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingsEntity {
	@JsonProperty("BaseURL")
	protected String baseUrl;

	@JsonProperty("BinaryVersion")
	protected String binaryVersion;

	@JsonProperty("CameraSupported")
	protected boolean cameraSupported;

	@JsonProperty("HideCamera")
	protected boolean hideCamera;

	@JsonProperty("LogonText")
	protected String logonText;

	@JsonProperty("MaxTransactionsMonths")
	protected int maxTransactionsMonths;

	@JsonProperty("UserTwoFactorLogon")
	protected boolean useTwoFactorLogon;

	@JsonProperty("Version")
	protected String version;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBinaryVersion() {
		return binaryVersion;
	}

	public void setBinaryVersion(String binaryVersion) {
		this.binaryVersion = binaryVersion;
	}

	public boolean isCameraSupported() {
		return cameraSupported;
	}

	public void setCameraSupported(boolean cameraSupported) {
		this.cameraSupported = cameraSupported;
	}

	public boolean isHideCamera() {
		return hideCamera;
	}

	public void setHideCamera(boolean hideCamera) {
		this.hideCamera = hideCamera;
	}

	public String getLogonText() {
		return logonText;
	}

	public void setLogonText(String logonText) {
		this.logonText = logonText;
	}

	public int getMaxTransactionsMonths() {
		return maxTransactionsMonths;
	}

	public void setMaxTransactionsMonths(int maxTransactionsMonths) {
		this.maxTransactionsMonths = maxTransactionsMonths;
	}

	public boolean isUseTwoFactorLogon() {
		return useTwoFactorLogon;
	}

	public void setUseTwoFactorLogon(boolean useTwoFactorLogon) {
		this.useTwoFactorLogon = useTwoFactorLogon;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
