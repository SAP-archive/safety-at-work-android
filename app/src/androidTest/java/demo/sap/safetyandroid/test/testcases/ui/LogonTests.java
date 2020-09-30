package demo.sap.safetyandroid.test.testcases.ui;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import demo.sap.safetyandroid.app.WelcomeActivity;
import demo.sap.safetyandroid.test.core.ClientPolicyManager;
import demo.sap.safetyandroid.test.core.BaseTest;
import demo.sap.safetyandroid.test.core.UIElements;
import demo.sap.safetyandroid.test.core.Utils;
import demo.sap.safetyandroid.test.core.Credentials;
import demo.sap.safetyandroid.test.core.WizardDevice;
import demo.sap.safetyandroid.test.pages.DetailPage;
import demo.sap.safetyandroid.test.pages.PasscodePage;
import demo.sap.safetyandroid.test.pages.EntityListPage;
import demo.sap.safetyandroid.test.pages.MasterPage;
import demo.sap.safetyandroid.test.pages.SettingsListPage;
import demo.sap.safetyandroid.test.pages.WelcomePage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static demo.sap.safetyandroid.test.core.UIElements.EntityListScreen.entityList;

@RunWith(AndroidJUnit4.class)
public class LogonTests extends BaseTest {

    @Rule
    public ActivityTestRule<WelcomeActivity> activityTestRule = new ActivityTestRule<>(WelcomeActivity.class);

    @Test
    public void testLogonFlow() {

        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding(activityTestRule.getActivity());

        // Actions on the entitylist Page
        EntityListPage entityListPage = new EntityListPage(entityList);
         entityListPage.clickFirstElement();
         entityListPage.leavePage();

        // Actions on the master Page
        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();
        masterPage.leavePage();

        DetailPage detailPage = new DetailPage();
        detailPage.clickBack();
        detailPage.leavePage();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();
        masterPage.leavePage();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage();
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }


    @Test
    public void logonFlowPutAppIntoBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding(activityTestRule.getActivity());

        EntityListPage entityListPage = new EntityListPage(entityList);
         entityListPage.clickFirstElement();
         entityListPage.leavePage();

        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();
        masterPage.leavePage();

        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ClientPolicyManager.getInstance().getPasscodeLockTimeout();

        // Put the app into background and immediately start again
        WizardDevice.putApplicationBackground(0, activityTestRule);
        WizardDevice.reopenApplication();

        if (lockTimeOut == 0) {
            PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
            enterPasscodePage.enterPasscode(Credentials.PASSCODE);
            enterPasscodePage.clickSignIn();
            enterPasscodePage.leavePage();
        }

        DetailPage mDetailPage = new DetailPage(UIElements.DetailScreen.deleteButton);
        mDetailPage.clickBack();
        mDetailPage.leavePage();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();
        masterPage.leavePage();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage();
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }
    @Test
    public void LogonFlowBack () {
        Utils.checkCredentials();
        WelcomePage welcomePage = new WelcomePage();
        welcomePage.clickGetStarted();
        welcomePage.waitForCredentials();
        Utils.pressBack();
        Utils.doOnboarding(activityTestRule.getActivity());
    }
}
