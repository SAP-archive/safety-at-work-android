package demo.sap.safetyandroid.test.core;

import demo.sap.safetyandroid.R;

public class UIElements {
    public static class WelcomePage {
        public static int getStartedButton = R.id.launchscreen_button_primary;
        public static int getStartedText = R.id.launchscreen_title;
    }

    public static class ActivationPage {
        public static int startButton = R.id.activationscreen_button_choose_discovery;
        public static int emailText = R.id.activation_email_address;
    }

    public static class LoginScreen {
        public static class BasicAuthScreen {
                public static String usernameID = "com.company.neowizardbasicauthpushtest:id/username";
                public static int usernameText = R.id.basic_auth_user_name_cell;
                public static int passwordText = R.id.basic_auth_password_cell;
                public static int okButton = R.id.basic_auth_screen_ok_button;
            }

        public static class OauthScreen {
            public static String oauthUsernameText = "j_username";
            public static String oauthPasswordText = "j_password";
            public static String oauthLogonButton = "logOnFormSubmit";
            public static String oauthAuthorizeButton = "buttonAuthorize";
        }
    }

    public static class PasscodeScreen {
        public static int createPasscodeText = R.id.passcode_field;
        public static int verifyPasscodeText = createPasscodeText;
        public static int enterPasscodeText = createPasscodeText;
        public static int nextButton = R.id.done_button;
        public static int secondNextButton = R.id.second_done_button;
        public static int cancelButton = R.id.skip_button;
        public static int useDefaultButton = R.id.skip_button;
        public static int reachedRetryLimitTitle = R.string.max_retries_title;
        public static int reachedRetryLimitMessage = R.string.max_attempts_reached_message;
        public static int backButton = R.id.cancel_button;
        public static int retryLimitDialog = R.id.action_bar_root;
        public static int resetAppButton = android.R.id.button2;
    }

    public static class SetFingerprintPage {
        //Set fingerprint page elements
        public static int confirmFingerprintLabel = R.id.confirm_fingerprint_detail_label;
        public static int skipFingerpintButton = R.id.confirm_fingerprint_try_password_button;
    }

    public static class EntityListScreen {
        // EntityListScreen elements
        public static int entityList = R.id.entity_list;
        public static String settingsToolBar = "More options"; //settingsToolCar invokes SettingsScreen
        public static int settingsText = R.string.settings_activity_name;
    }

    public static class MasterScreen {
        public static int listView = R.id.item_list;
        public static int refreshButton = R.id.menu_refresh;
        public static int addButton = R.id.fab; //addButton invokes AddOrUpdateItemScreen
        public static String toolBarBackButton = "Navigate up";
        public static int floatingActionButton = R.id.fab;
    }

    public static class DetailScreen {
        public static int updateButton = R.id.update_item; //updateButton invokes AddOrUpdateItemScreen
        public static int deleteButton = R.id.delete_item;
        public static String toolBarBackButton = "Navigate up";
        public static int cancelButton = android.R.id.button2;
        public static int okButton = android.R.id.button1;
        public static int saveButton = R.id.save_item;
    }

    public static class SettingsScreen {
        public static int settingsButton = R.id.settings_container;
        public static int settingsList = R.id.recycler_view;
        public static int resetApp = R.string.reset_app;
        public static int logLevel = R.string.log_level;
        public static int logUpload = R.string.upload_log;
        public static int managePasscode = R.string.manage_passcode;
        public static int logLevelValue = android.R.id.summary;
        public static int yesButtonResetApp = android.R.id.button1;
        public static int noButtonResetApp = android.R.id.button2;
    }

    public static class AddOrUpdateItemScreen {
        public static int addButton = R.id.fab;

    }

    public static class EulaScreen{
        public static int eulaDisagreeBtn = R.id.eula_reject;
        public static int eulaAgreeBtn = R.id.eula_confirm;
    }

    public static class ErrorScreen {
        public static String messageResourceId = "android:id/message";
        public static int messageId = android.R.id.message;
        public static int okButton = android.R.id.button1;
    }

    public static class NoUIScreen{
        public static String helloWorldTextID = "Hello World!";
    }

}
