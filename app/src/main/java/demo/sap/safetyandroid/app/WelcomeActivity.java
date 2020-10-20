package demo.sap.safetyandroid.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import demo.sap.safetyandroid.R;
import com.sap.cloud.mobile.fiori.onboarding.LaunchScreen;
import com.sap.cloud.mobile.fiori.onboarding.ext.LaunchScreenSettings;
import com.sap.cloud.mobile.flowv2.core.Flow;
import com.sap.cloud.mobile.flowv2.model.FlowType;
import com.sap.cloud.mobile.flowv2.model.AppConfig;
import com.sap.cloud.mobile.flowv2.model.FlowConstants;
import com.sap.cloud.mobile.flowv2.core.FlowContext;
import com.sap.cloud.mobile.flowv2.core.FlowContextBuilder;
import com.sap.cloud.mobile.flowv2.model.OAuth;
import com.sap.cloud.mobile.flowv2.model.OAuthClient;
import com.sap.cloud.mobile.flowv2.model.OAuthConfig;
import java.net.URL;
import java.net.MalformedURLException;

import com.sap.cloud.mobile.foundation.logging.Logging;
import com.sap.cloud.mobile.foundation.logging.LogService;
import ch.qos.logback.classic.Level;

import java.util.ArrayList;
import java.util.List;

import demo.sap.safetyandroid.app.storage.SecureStorage;
import kotlin.jvm.JvmClassMappingKt;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeActivity extends AppCompatActivity {
    private boolean isFlowStarted = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LaunchScreen welcome = new LaunchScreen(this);
        welcome.initialize(new LaunchScreenSettings.Builder()
                .setDemoButtonVisible(false)
                .setHeaderLineLabel(getString(R.string.welcome_screen_headline_label))
                .setPrimaryButtonText(getString(R.string.welcome_screen_primary_button_label))
                .setFooterVisible(true)
                .setUrlTermsOfService("http://www.sap.com")
                .setUrlPrivacy("http://www.sap.com")
                .addInfoViewSettings(
                        new LaunchScreenSettings.LaunchScreenInfoViewSettings(
                                R.drawable.logo2,
                                getString(R.string.application_name),
                                getString(R.string.welcome_screen_detail_label)
                        )
                )
                .build());
        welcome.setPrimaryButtonOnClickListener(v -> {
            if (!isFlowStarted) {
                startFlow(this, FlowType.ONBOARDING);
                isFlowStarted = true;
            }
        });

   //     setContentView(welcome);
       SecureStorage sec = SecureStorage.getInstance(this);

       if(sec!=null && sec.getOnboardingCompleted()){
           Log.i("WELCOME","ONBAORDED");
           if (!isFlowStarted) {
               startFlow(this, FlowType.ONBOARDING);
               isFlowStarted = true;
           }
       }
        else {
           setContentView(welcome);
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FlowConstants.FLOW_ACTIVITY_REQUEST_CODE) {
            isFlowStarted = false;
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, MainBusinessActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public static void startFlow(Activity context, FlowType flowType) {
        try {
            FlowContext flowContext = new FlowContextBuilder()
                    .setApplication(prepareAppConfig())
                    .setMobileServices(getServices())
                    .setFlowStateListener(new WizardFlowStateListener(
                            (SAPWizardApplication) context.getApplication()))
                    .build();
            Flow.start(context, flowContext);
        } catch (MalformedURLException ex) {
            //Do nothing, should not happen since wizard will check the URL format
        }
    }

    private static List getServices() {
        List services = new ArrayList<>();
        Logging.setConfigurationBuilder(new Logging.ConfigurationBuilder().initialLevel(Level.WARN).logToConsole(true).build());
        services.add(JvmClassMappingKt.getKotlinClass(LogService.class));
        return services;
    }

    private static AppConfig prepareAppConfig() throws MalformedURLException {
        OAuthConfig oauthConfig = new OAuthConfig.Builder()
                .authorizationEndpoint("https://sawprod-italy-it-sap-safetyatwork.cfapps.eu10.hana.ondemand.com/oauth2/api/v1/authorize")
                .tokenEndpoint("https://sawprod-italy-it-sap-safetyatwork.cfapps.eu10.hana.ondemand.com/oauth2/api/v1/token")
                .addClient(new OAuthClient.Builder().clientID("e4a7d9b8-c98d-49af-b9ea-b9696235164a").redirectURL("https://sawprod-italy-it-sap-safetyatwork.cfapps.eu10.hana.ondemand.com").grantType("code").build())
                .build();

        return new AppConfig.Builder()
                .applicationId("it.sap.safetyatwork")
                .host(new URL("https://sawprod-italy-it-sap-safetyatwork.cfapps.eu10.hana.ondemand.com/").getHost())
                .addAuth(new OAuth.Builder().config(oauthConfig).build())
                .build();
        }
}