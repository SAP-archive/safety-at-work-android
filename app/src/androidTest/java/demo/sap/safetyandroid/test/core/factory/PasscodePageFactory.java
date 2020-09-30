package demo.sap.safetyandroid.test.core.factory;

import android.os.SystemClock;

import demo.sap.safetyandroid.app.SAPWizardApplication;
import demo.sap.safetyandroid.test.core.ClientPolicy;
import demo.sap.safetyandroid.test.core.ClientPolicyManager;
import demo.sap.safetyandroid.test.core.Credentials;
import demo.sap.safetyandroid.test.core.Utils;
import demo.sap.safetyandroid.test.pages.PasscodePage;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

public class PasscodePageFactory {

    public static void PasscodeFlow() {
        checkServiceUrl();

        // Get the current clientpolicy
        ClientPolicy clientPolicy = ClientPolicyManager.getInstance().getClientPolicy();
        // If there is a passcode policy
        if (clientPolicy.isPasscodePolicyEnabled()) {
            // Actions on the passcode Page
            PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();

            // Actions on the verifypasscode Page
            PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickNext();
            verifyPasscodePage.leavePage();

            // Skip Fingerprint
            Utils.skipFingerprint();


        } else {
            // we skip the passcode flow
        }

    }

    public static void PasscodeFlowBack() {
        checkServiceUrl();

        // Get the current clientpolicy
        ClientPolicy clientPolicy = ClientPolicyManager.getInstance().getClientPolicy();
        // If there is a passcode policy
        if (clientPolicy.isPasscodePolicyEnabled()) {
            // Actions on the passcode Page
            PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();

            // Actions on the verifypasscode Page
            PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickBack();
            verifyPasscodePage.leavePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickNext();
            verifyPasscodePage.leavePage();

            // Skip Fingerprint
            Utils.skipFingerprint();


        } else {
            // we skip the passcode flow
        }

    }

    public static void NewPasscodeFlow() {
        checkServiceUrl();

        // Get the current clientpolicy
        ClientPolicyManager.getInstance().getClientPolicy();

        PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickSignIn();
        verifyPasscodePage.leavePage();
    }

    public static void NewPasscodeFlowBack() {
        checkServiceUrl();

        // Get the current clientpolicy
        ClientPolicyManager.getInstance().getClientPolicy();

        PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickBack();
        verifyPasscodePage.leavePage();

        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickSignIn();
        verifyPasscodePage.leavePage();
    }

    private static void checkServiceUrl() {
        // Wait for service url until one minute
        int limit = 60;
        SAPWizardApplication application = (SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext();
        while (limit > 0 && application.getAppConfig().getServiceUrl() == null) {
            SystemClock.sleep(1000);
            limit--;
        }
    }

}