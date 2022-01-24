package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

public enum MitIdLocator {
    /*
    Login screen
    */
    LOC_USERNAME_INPUT,
    LOC_CONTINUE_BUTTON,

    /*
    Code app 2FA screen
     */
    LOC_CODE_APP_SCREEN_TITLE,

    /*
    Enter password 2FA screen
    */
    LOC_ENTER_PASSWORD_INPUT,

    /*
    Common 2FA screen elements
     */
    LOC_CHANGE_AUTH_METHOD_LINK,

    /*
    2FA method selector
     */
    LOC_CHOOSE_METHOD_TITLE,
    LOC_CHOOSE_CODE_APP_BUTTON,
    LOC_CHOOSE_CODE_DISPLAY_BUTTON,
    LOC_CHOOSE_CODE_CHIP_BUTTON,

    /*
    Error screen
     */
    LOC_TRY_AGAIN_BUTTON,
    LOC_ERROR_NOTIFICATION,

    /*
    CPR screen
     */
    LOC_CPR_INPUT,
    LOC_CPR_BUTTON_OK
}
