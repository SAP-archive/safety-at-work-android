package demo.sap.safetyandroid.test.core;

import com.sap.cloud.mobile.foundation.settings.Settings;
import com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import ch.qos.logback.classic.Level;

public class ClientPolicyManager {

    public static final String KEY_RETRY_COUNT = "retryCount";
    public static final String KEY_PC_WAS_SET_AT = "when_was_the_pc_set";

    private static final String PASSCODE_POLICY_FINGERPRINT_ENABLED = "passwordPolicyFingerprintEnabled";
    private static final String PASSCODE_POLICY_DIGIT_REQUIRED = "passwordPolicyDigitRequired";
    private static final String PASSCODE_POLICY_LOWER_REQUIRED = "passwordPolicyLowerRequired";
    private static final String PASSCODE_POLICY_SPECIAL_REQUIRED = "passwordPolicySpecialRequired";
    private static final String PASSCODE_POLICY_UPPER_REQUIRED = "passwordPolicyUpperRequired";
    private static final String PASSCODE_POLICY_MIN_LENGTH = "passwordPolicyMinLength";
    private static final String PASSCODE_POLICY_MIN_UNIQUE_CHARS = "passwordPolicyMinUniqueChars";
    private static final String PASSCODE_POLICY_RETRY_LIMIT = "passwordPolicyRetryLimit";
    private static final String PASSCODE_POLICY_IS_DIGITS_ONLY = "passwordPolicyIsDigitsOnly";
    private static final String PASSCODE_POLICY_ENABLED = "passwordPolicyEnabled";
    private static final String PASSCODE_POLICY_LOCK_TIMEOUT = "passwordPolicyLockTimeout";
    private static final String PASSCODE_POLICY_EXPIRES_IN_N_DAYS = "passwordPolicyExpiresInNDays";

    private static final String LOG_POLICY_LOG_LEVEL = "logLevel";
    private static final String LOG_POLICY_LOG_ENABLED = "logEnabled";

    private static final String SETTINGS_PASSCODE = "passwordPolicy";
    private static final String SETTINGS_LOG = "logSettings";

    private static final String KEY_CLIENT_POLICY = "passcodePolicy";

    public static final String KEY_CLIENT_LOG_LEVEL = "client_log_level";

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPolicyManager.class);
    private ArrayList<LogLevelChangeListener> logLevelChangeListeners = new ArrayList<>();
    private static ClientPolicyManager instance = new ClientPolicyManager();

    private static ClientPolicy lastPolicy;
    private static ClientPolicy policyFromServer;

    private int passcodeLockTimeout;
    private int passcodeExpirationTimeFrame;

    private ClientPolicyManager(){

    }

    public static ClientPolicyManager getInstance(){
        return instance;
    }
    /**
     * Gets the client policies, including the passcode policy and the logging policy.
     * @return
     */
    public ClientPolicy getClientPolicy() {
        if(policyFromServer == null){
            getClientPolicyFromServer();
        }
        return policyFromServer;
    }


    private  void getClientPolicyFromServer() {

        policyFromServer = null;

        CountDownLatch downloadLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            Settings settings = new Settings();
            settings.load(Settings.SettingTarget.DEVICE, "mobileservices/settingsExchange", new PolicyCallbackListener(downloadLatch));
        });

        executor.shutdown();
        try {
            downloadLatch.await();
        } catch (InterruptedException e) {
             LOGGER.error("Unexpected interruption during client policy download", e);
             Thread.currentThread().interrupt();
        }
    }



    private  Level logLevelFromServerString(String logLevel) {
        String lowerCaseLogLevel = logLevel.toLowerCase(Locale.getDefault());
        if (lowerCaseLogLevel.equals("none")) {
            return Level.OFF;
        } else if (lowerCaseLogLevel.equals("fatal")) {
            return Level.ERROR;
        } else if (lowerCaseLogLevel.equals("error")) {
            return Level.ERROR;
        } else if (lowerCaseLogLevel.startsWith("warn")) {
            // use startsWith so this matches both the server-provided string WARN, and the user-friendly string Warning.
            return  Level.WARN;
        } else if (lowerCaseLogLevel.equals("info")) {
            return Level.INFO;
        } else if (lowerCaseLogLevel.equals("debug")) {
            return Level.DEBUG;
        } else if (lowerCaseLogLevel.equals("path")) {
            return Level.ALL;
        }
        return Level.DEBUG;
    }

  public void setClientPolicy(JSONObject jsonObject){
        JSONObject passcodePolicyJson = jsonObject.optJSONObject(SETTINGS_PASSCODE);
        if (passcodePolicyJson != null) {
            policyFromServer = new ClientPolicy();
            boolean isPasscodePolicyEnabled = passcodePolicyJson.optBoolean(PASSCODE_POLICY_ENABLED, true);
            policyFromServer.setPasscodePolicyEnabled(isPasscodePolicyEnabled);

            PasscodePolicy passcodePolicy = new PasscodePolicy();
            passcodePolicy.setAllowsFingerprint(passcodePolicyJson.optBoolean(PASSCODE_POLICY_FINGERPRINT_ENABLED, true));
            passcodePolicy.setHasDigit(passcodePolicyJson.optBoolean(PASSCODE_POLICY_DIGIT_REQUIRED, false));
            passcodePolicy.setHasLower(passcodePolicyJson.optBoolean(PASSCODE_POLICY_LOWER_REQUIRED, false));
            passcodePolicy.setHasSpecial(passcodePolicyJson.optBoolean(PASSCODE_POLICY_SPECIAL_REQUIRED, false));
            passcodePolicy.setHasUpper(passcodePolicyJson.optBoolean(PASSCODE_POLICY_UPPER_REQUIRED, false));
            passcodePolicy.setIsDigitsOnly(passcodePolicyJson.optBoolean(PASSCODE_POLICY_IS_DIGITS_ONLY, false)); // Is this actually set on the server??
            passcodePolicy.setMinLength(passcodePolicyJson.optInt(PASSCODE_POLICY_MIN_LENGTH, 8));
            passcodePolicy.setMinUniqueChars(passcodePolicyJson.optInt(PASSCODE_POLICY_MIN_UNIQUE_CHARS, 0));
            passcodePolicy.setRetryLimit(passcodePolicyJson.optInt(PASSCODE_POLICY_RETRY_LIMIT, 20));
            // if policy were enabled, then no default would be allowed
            setPasscodeExpirationTimeFrame(passcodePolicyJson.optInt(PASSCODE_POLICY_EXPIRES_IN_N_DAYS));
            setPasscodeLockTimeout(passcodePolicyJson.optInt(PASSCODE_POLICY_LOCK_TIMEOUT));
            passcodePolicy.setSkipEnabled(false);
            policyFromServer.setPasscodePolicy(passcodePolicy);
        }

        JSONObject logSettingsJson = jsonObject.optJSONObject(SETTINGS_LOG);
        if (logSettingsJson != null) {
            boolean isLogEnabled = logSettingsJson.optBoolean(LOG_POLICY_LOG_ENABLED, false);
            policyFromServer.setLogEnabled(isLogEnabled);
            if (isLogEnabled) {
                String logLevelStr = logSettingsJson.optString(LOG_POLICY_LOG_LEVEL, "DEBUG");
                Level logLevel = logLevelFromServerString(logLevelStr);
                policyFromServer.setLogLevel(logLevel);
            }
        }

        lastPolicy = policyFromServer;
    }

    private  class PolicyCallbackListener implements Settings.CallbackListener {

        private CountDownLatch downloadLatch;

        public PolicyCallbackListener(CountDownLatch downloadLatch) {
            this.downloadLatch = downloadLatch;
        }

        @Override
        public void onSuccess(@NonNull JSONObject result) {
            setClientPolicy(result);
            downloadLatch.countDown();
        }

        @Override
        public void onError(@NonNull Throwable throwable) {
            policyFromServer = null;
            LOGGER.error("Could not download the policy from the server due to error: " + throwable.getMessage());
            downloadLatch.countDown();
        }
    }

    public int getPasscodeLockTimeout() {
        return passcodeLockTimeout;
    }

    public void setPasscodeLockTimeout(int passcodeLockTimeout) {
        this.passcodeLockTimeout = passcodeLockTimeout;
    }

    public int getPasscodeExpirationTimeFrame() {
        return passcodeExpirationTimeFrame;
    }

    public void setPasscodeExpirationTimeFrame(int passcodeExpirationTimeFrame) {
        this.passcodeExpirationTimeFrame = passcodeExpirationTimeFrame;
    }

    public interface LogLevelChangeListener {
        void logLevelChanged(Level newLogLevel);
    }

}
