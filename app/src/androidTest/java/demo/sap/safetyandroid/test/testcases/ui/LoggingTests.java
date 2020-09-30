package demo.sap.safetyandroid.test.testcases.ui;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import demo.sap.safetyandroid.app.WelcomeActivity;
import demo.sap.safetyandroid.test.core.BaseTest;
import demo.sap.safetyandroid.test.core.Utils;
import demo.sap.safetyandroid.test.pages.EntityListPage;
import demo.sap.safetyandroid.test.pages.SettingsListPage;
import demo.sap.safetyandroid.test.core.ClientPolicyManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import ch.qos.logback.classic.Level;

@RunWith(AndroidJUnit4.class)
public class LoggingTests extends BaseTest {

    @Rule
    public ActivityTestRule<WelcomeActivity> activityTestRule = new ActivityTestRule<>(WelcomeActivity.class);

    private static final String DEFAULT_LOG_LEVEL = "Warn";

    @Test
    public void testLogPolicyValue() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding(activityTestRule.getActivity());

        EntityListPage entityListPage = new EntityListPage();
        entityListPage.clickSettings();
        entityListPage.leavePage();

        // Check log level value
        Level policyLevel = ClientPolicyManager.getInstance().getClientPolicy().getLogLevel();
        String policyString;
        // If the policyLevel is null it means there is no policy set on the server
        if (policyLevel == null) {
            policyString = DEFAULT_LOG_LEVEL;
        } else {
            policyString = (policyLevel.levelStr.toLowerCase());
            policyString = policyString.substring(0, 1).toUpperCase() + policyString.substring(1);
            if (policyString.equals("All")) {
                policyString = "Path";
            }
            if (policyString.equals("Off")) {
                policyString = "None";
            }

        }
        SettingsListPage settingsListPage = new SettingsListPage();
        settingsListPage.checkLoglevel(policyString);
    }

}
