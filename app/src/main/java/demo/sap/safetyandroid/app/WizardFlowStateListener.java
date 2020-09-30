package demo.sap.safetyandroid.app;

import android.content.Intent;
import android.util.Log;
import javax.crypto.Cipher;
import com.sap.cloud.mobile.flowv2.ext.FlowStateListener;
import com.sap.cloud.mobile.flowv2.model.AppConfig;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;
import com.sap.cloud.mobile.flowv2.model.LogSettings;
import ch.qos.logback.classic.Level;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import demo.sap.safetyandroid.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.NotNull;

public class WizardFlowStateListener extends FlowStateListener {
    private static Logger logger = LoggerFactory.getLogger(WizardFlowStateListener.class);
    private SAPWizardApplication application;

    public WizardFlowStateListener(@NotNull SAPWizardApplication application) {
        super();
        this.application = application;
    }

    @Override
    public void onAppConfigRetrieved(@NotNull AppConfig appConfig) {
        Log.d(WizardFlowStateListener.class.getSimpleName(), "onAppConfigRetrieved " + appConfig.toString());
        application.initializeServiceManager(appConfig);
        application.setAppConfig(appConfig);
    }

    @Override
    public void onApplicationReset() {
        Log.d(WizardFlowStateListener.class.getSimpleName(), "onApplicationReset executing...");
        this.application.resetApp();
        Intent intent = new Intent(application, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        application.startActivity(intent);
    }

    @Override
    public void onApplicationLocked() {
        super.onApplicationLocked();
        application.isApplicationUnlocked = false;
    }

    @Override
    public void onUnlockWithCipher(@NotNull Cipher cipher) {
        super.onUnlockWithCipher(cipher);
        application.isApplicationUnlocked = true;
    }

    @Override
    public void onUnlockWithPasscode(@NotNull char[] code) {
        super.onUnlockWithPasscode(code);
        application.isApplicationUnlocked = true;
    }

    @Override
    public void onBoarded() {
        super.onBoarded();
        application.isApplicationUnlocked = true;
    }
    @Override
    public void onLogSettingsRetrieved(@NotNull LogSettings logSettings) {
        Log.d(WizardFlowStateListener.class.getSimpleName(), "onLogSettingsRetrieved: " + logSettings.toString());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
        String logString = sp.getString(SAPWizardApplication.KEY_LOG_SETTING_PREFERENCE, "");
        LogSettings currentSettings;
        if (logString.isEmpty()) {
            currentSettings = new LogSettings();
        } else {
            currentSettings = LogSettings.createFromJsonString(logString);
        }

        if (!currentSettings.getLogLevel().equals(logSettings.getLogLevel()) || logString.isEmpty()) {
            sp.edit().putString(SAPWizardApplication.KEY_LOG_SETTING_PREFERENCE,
                    logSettings.toString()).apply();
            LogSettings.setRootLogLevel(logSettings);
            AppLifecycleCallbackHandler.getInstance().getActivity().runOnUiThread(() -> {
                Map mapping = new HashMap<Level, String>();
                mapping.put(Level.ALL, application.getString(R.string.log_level_path));
                mapping.put(Level.DEBUG, application.getString(R.string.log_level_debug));
                mapping.put(Level.INFO, application.getString(R.string.log_level_info));
                mapping.put(Level.WARN, application.getString(R.string.log_level_warning));
                mapping.put(Level.ERROR, application.getString(R.string.log_level_error));
                mapping.put(Level.OFF, application.getString(R.string.log_level_none));
                Toast.makeText(
                        application,
                        String.format(
                                application.getString(R.string.log_level_changed),
                                mapping.get(LogSettings.getLogLevel(logSettings))
                        ),
                        Toast.LENGTH_SHORT
                ).show();
                logger.info(String.format(
                                application.getString(R.string.log_level_changed),
                                mapping.get(LogSettings.getLogLevel(logSettings))
                        ));
            });
        }
    }

}
