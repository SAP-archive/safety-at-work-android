package demo.sap.safetyandroid.mdui;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.annotation.Nullable;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.app.SAPWizardApplication;

import demo.sap.safetyandroid.app.WizardFlowStateListener;
import com.sap.cloud.mobile.flowv2.core.Flow;
import com.sap.cloud.mobile.flowv2.core.FlowContext;
import com.sap.cloud.mobile.flowv2.core.FlowContextBuilder;
import com.sap.cloud.mobile.flowv2.model.FlowConstants;
import com.sap.cloud.mobile.flowv2.model.FlowType;
import com.sap.cloud.mobile.flowv2.core.DialogHelper;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import androidx.preference.ListPreference;
import ch.qos.logback.classic.Level;
import com.sap.cloud.mobile.flowv2.model.LogSettings;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import com.sap.cloud.mobile.foundation.logging.Logging;

import android.widget.Toast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import kotlin.Unit;

/** This fragment represents the settings screen. */
public class SettingsFragment extends PreferenceFragmentCompat implements Logging.UploadListener {

    private ListPreference logLevelPreference;
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsFragment.class);
    private Preference changePasscodePreference;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        SAPWizardApplication application =
            (SAPWizardApplication) getActivity().getApplication();
        addPreferencesFromResource(R.xml.preferences);
        logLevelPreference = (ListPreference) findPreference(getString(R.string.log_level));
        prepareLogLevelSetting(logLevelPreference);

        // Upload log
        final Preference logUploadPreference = findPreference(getString(R.string.upload_log));
        logUploadPreference.setOnPreferenceClickListener(preference -> {
            logUploadPreference.setEnabled(false);
            Logging.upload();
            return false;
        });

        changePasscodePreference = findPreference(getString(R.string.manage_passcode));
        changePasscodePreference.setOnPreferenceClickListener(preference -> {
            changePasscodePreference.setEnabled(false);
            FlowContext flowContext = new FlowContextBuilder()
                    .setApplication(application.getAppConfig())
                    .setFlowType(FlowType.CHANGEPASSCODE)
                    .setFlowStateListener(new WizardFlowStateListener(application))
                    .build();
            Flow.start(this, flowContext);
            return false;
        });


        // Reset App
        Preference resetAppPreference = findPreference(getString(R.string.reset_app));
        resetAppPreference.setOnPreferenceClickListener(preference -> {
            new DialogHelper(getContext(), R.style.OnboardingDefaultTheme_Dialog_Alert)
                    .showDialogWithCancelAction(
                            getActivity().getSupportFragmentManager(),
                            requireContext().getString(R.string.reset_app_confirmation),
                            (() -> {
                                return Unit.INSTANCE;
                            }),
                            getString(R.string.confirm_no),
                            getString(R.string.reset_app),
                            getString(R.string.confirm_yes),
                            (() -> {
                                FlowContext flowContext = new FlowContextBuilder()
                                        .setApplication(application.getAppConfig())
                                        .setFlowStateListener(new WizardFlowStateListener(application))
                                        .setFlowType(FlowType.RESET)
                                        .build();
                                Flow.start(this, flowContext);
                                return Unit.INSTANCE;
                            })
                    );
            return false;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FlowConstants.FLOW_ACTIVITY_REQUEST_CODE) {
            changePasscodePreference.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logging.addUploadListener(this);
        logLevelPreference = findPreference(getString(R.string.log_level));
        prepareLogLevelSetting(logLevelPreference);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logging.removeUploadListener(this);
    }

    @Override
    public void onSuccess() {
        enableLogUploadButton();
        Toast.makeText(getActivity(), R.string.log_upload_ok, Toast.LENGTH_LONG).show();
        LOGGER.info("Log is uploaded to the server.");
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        enableLogUploadButton();
        new DialogHelper(getActivity(), R.style.OnboardingDefaultTheme_Dialog_Alert)
                .showOKOnlyDialog(
                        getActivity().getSupportFragmentManager(),
                        throwable.getMessage(),
                        null, null, null
                );
        LOGGER.error("Log upload failed with error message: " + throwable.getLocalizedMessage());
    }

    @Override
    public void onProgress(int i) {
        // You could add a progress indicator and update it from here
    }

    private void enableLogUploadButton() {
        final Preference logUploadPreference = findPreference(getString(R.string.upload_log));
        logUploadPreference.setEnabled(true);
    }


    private void prepareLogLevelSetting(ListPreference listPreference) {
        Map<Level,String> mapping = getLevelStrings();
        logLevelPreference.setEntries(mapping.values().toArray(new String[0]));
        logLevelPreference.setEntryValues(getLevelValues());
        logLevelPreference.setPersistent(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String logString = sp.getString(SAPWizardApplication.KEY_LOG_SETTING_PREFERENCE, new LogSettings().toString());
        LogSettings settings = LogSettings.createFromJsonString(logString);

        logLevelPreference.setSummary(mapping.get(LogSettings.getLogLevel(settings)));
        logLevelPreference.setValue(String.valueOf(LogSettings.getLogLevel(settings).levelInt));
        logLevelPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            // Get the new value
            Level logLevel = Level.toLevel(Integer.valueOf((String) newValue));
            LogSettings newSettings = settings.copy(
                    settings.getEnabled(), settings.getMaxFileSize(),
                    LogSettings.getLogLevelString(logLevel),
                    settings.getEntryExpiry(), settings.getMaxFileNumber()
            );
            sp.edit().putString(SAPWizardApplication.KEY_LOG_SETTING_PREFERENCE, newSettings.toString()).apply();
            LogSettings.setRootLogLevel(newSettings);
            preference.setSummary(mapping.get(LogSettings.getLogLevel(newSettings)));

            return true;
        });
    }

    private Map<Level, String> getLevelStrings() {
        Map mapping = new LinkedHashMap<Level, String>();
        mapping.put(Level.ALL, getString(R.string.log_level_path));
        mapping.put(Level.DEBUG, getString(R.string.log_level_debug));
        mapping.put(Level.INFO, getString(R.string.log_level_info));
        mapping.put(Level.WARN, getString(R.string.log_level_warning));
        mapping.put(Level.ERROR, getString(R.string.log_level_error));
        mapping.put(Level.OFF, getString(R.string.log_level_none));
        return mapping;
    }

    private String[] getLevelValues() {
        return new String[]{
                String.valueOf(Level.ALL.levelInt),
                String.valueOf(Level.DEBUG.levelInt),
                String.valueOf(Level.INFO.levelInt),
                String.valueOf(Level.WARN.levelInt),
                String.valueOf(Level.ERROR.levelInt),
                String.valueOf(Level.OFF.levelInt)};
    }
}
