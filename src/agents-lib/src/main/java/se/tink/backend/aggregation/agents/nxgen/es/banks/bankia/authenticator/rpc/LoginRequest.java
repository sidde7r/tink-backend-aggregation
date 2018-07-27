package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LoginRequest extends AbstractForm {

    private LoginRequest(PersistentStorage persistentStorage, String username, String password, String execution,
            String encryptedPassword) {
        this.put(BankiaConstants.Form.J_GID_ID_DISPOSITIVO,
                persistentStorage.get(BankiaConstants.StorageKey.DEVICE_ID_BASE_64));
        this.put(BankiaConstants.Form.ID_DISPOSITIVO,
                persistentStorage.get(BankiaConstants.StorageKey.DEVICE_ID_BASE_64_URL));
        this.put(BankiaConstants.Form.J_GID_COD_APP, BankiaConstants.Default.OMP);
        this.put(BankiaConstants.Form.J_GID_COD_DS, BankiaConstants.Default.UPPER_CASE_OIP);
        this.put(BankiaConstants.Form.J_GID_ACTION, BankiaConstants.Default.LOGIN);
        this.put(BankiaConstants.Form.J_GID_PASSWORD, encryptedPassword);
        this.put(BankiaConstants.Form.CONTRASENA, password);
        this.put(BankiaConstants.Form.EVENT_ID, BankiaConstants.Default.COMPROBAR_IDENTIFICACION);
        this.put(BankiaConstants.Form.ORIGEN, BankiaConstants.Default.OM);
        this.put(BankiaConstants.Form.J_GID_NUM_DOCUMENTO, username);
        this.put(BankiaConstants.Form.IDENTIFICADOR, username);
        this.put(BankiaConstants.Form.EXECUTION, execution);
        this.put(BankiaConstants.Form.VERSION, BankiaConstants.Default._5_0);
        this.put(BankiaConstants.Form.TIPO, BankiaConstants.Default.ANDROID_PHONE);
    }

    public static LoginRequest create(PersistentStorage persistentStorage, String username, String password,
            String execution, String encryptedPassword) {
        return new LoginRequest(persistentStorage, username, password, execution, encryptedPassword);
    }
}
