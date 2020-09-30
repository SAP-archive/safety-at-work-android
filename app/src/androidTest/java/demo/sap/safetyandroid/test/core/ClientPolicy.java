package demo.sap.safetyandroid.test.core;

import com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy;

import java.io.Serializable;

import ch.qos.logback.classic.Level;

/**
 * Wrapper class which contains the client policies, which could arrive from the server. It contains
 * the {@link com.sap.cloud.mobile.onboarding.passcode.PasscodePolicy}, the log settings and some
 * boolean flags, e.g. whether passode policy is enabled.
 */
public class ClientPolicy implements Serializable{

    private static final long serialVersionUID = 1;

    private boolean isPasscodePolicyEnabled;
    private PasscodePolicy passcodePolicy;
    private Level logLevel;
    private boolean isLogEnabled;

    public boolean isPasscodePolicyEnabled() {
        return isPasscodePolicyEnabled;
    }

    public void setPasscodePolicyEnabled(boolean passcodePolicyEnabled) {
        isPasscodePolicyEnabled = passcodePolicyEnabled;
    }

    public PasscodePolicy getPasscodePolicy() {
        return passcodePolicy;
    }

    public void setPasscodePolicy(PasscodePolicy passcodePolicy) {
        this.passcodePolicy = passcodePolicy;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isLogEnabled() {
        return isLogEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        isLogEnabled = logEnabled;
    }
}
