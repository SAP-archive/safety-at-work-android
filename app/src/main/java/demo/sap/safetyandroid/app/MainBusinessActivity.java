package demo.sap.safetyandroid.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sap.cloud.android.odata.v2.DeviceSetType;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.foundation.common.SettingsProvider;
import com.sap.cloud.mobile.foundation.mobileservices.MobileServices;
import com.sap.cloud.mobile.foundation.networking.HttpException;
import com.sap.cloud.mobile.foundation.user.UserInfo;
import com.sap.cloud.mobile.foundation.user.UserRoleService;
import com.sap.cloud.mobile.foundation.user.UserRoles;
import com.sap.cloud.mobile.odata.LocalDateTime;


import org.dpppt.android.sdk.internal.AppConfigManager;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.app.storage.SecureStorage;
import demo.sap.safetyandroid.mdui.EntitySetListActivity;
import demo.sap.safetyandroid.service.SAPServiceManager;


public class MainBusinessActivity extends AppCompatActivity {

    private static final int REQ_ONBOARDING = 123;
    private SecureStorage secureStorage;

    public static final String ACTION_GOTO_REPORTS = "ACTION_GOTO_REPORTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_business);
        secureStorage = SecureStorage.getInstance(this);

        startEntitySetListActivity();
    }

    private void startEntitySetListActivity() {
        SAPWizardApplication application = (SAPWizardApplication) getApplication();
        application.getSAPServiceManager().openODataStore(() -> {
//            Intent intent = new Intent(this, OnboardingActivity.class);
//
//
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(intent);

            boolean onboardingCompleted = secureStorage.getOnboardingCompleted();
            if (onboardingCompleted) {
                Intent intent = new Intent(this, TraceReportActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {

                startActivityForResult(new Intent(this, OnboardingActivity.class), REQ_ONBOARDING);

            }
//            UserRoleService.addChangeListenner((oldInfo, newInfo) -> {
//                //hanlde the downloaded user information
//                Log.d(newInfo.getUserName(),"username");
//            });
//            MobileServices.start(this.getApplication(), ClientProvider.get(), SettingsProvider.get(), UserRoleService.class);

            UserRoles roles = new UserRoles();

            roles.load(new UserRoles.CallbackListener() {

                           @Override
                           public void onSuccess(UserInfo o) {
        /*
        Here goes the code for processing successful response.
        UserInfo contains the user-id, username, and user-roles
        UserInfo is a parcelable object so, if you wish, this can
        be passed through bundle to different activities
        */
                               Log.i("User Name", o.getUserName());
                               Log.i("User Id", o.getId());
//                               String[] roles = o.getRoles();
//                               Log.i("UserInfo: ", "User has the following Roles");
//                               for (int i = 0; i < roles.length; i++) {
//                                   Log.i("Role Name", roles[i]);
//                               }
                               SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
                               sharedPref.edit().putString("username", o.getId()).apply();
                               //create devietype on SCP

                               // Proxy Class
                               DeviceSetType mydevice = new DeviceSetType();
// Using type specific setters to set the property value
                               mydevice.setType_("USER");
                               mydevice.setDescription("ANDROID Device");
                               mydevice.setDeviceID(o.getId());

                               mydevice.setCreatedAt(LocalDateTime.now());
                               mydevice.setUpdatedAt(LocalDateTime.now());
                               mydevice.setCreatedBy("");
                               mydevice.setUpdatedBy("");
                               mydevice.setOwnedBy(new DeviceSetType());
                               mydevice.setOwnedByID("");
                               mydevice.setCapacity(0);
                               mydevice.setMajor("");
                               mydevice.setMinor("");

                               if (android.os.Build.VERSION.SDK_INT > 9)
                               {
                                   StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                   StrictMode.setThreadPolicy(policy);
                               }
                               SAPServiceManager sapServiceManager = ((SAPWizardApplication) getApplication()).getSAPServiceManager();
try {
    sapServiceManager.getv2().createEntity(mydevice);
} catch (Exception e) {
    e.printStackTrace();
    Log.e("SEND DEVICE",e.getLocalizedMessage());
}

                           }

                           @Override
                           public void onError(Throwable result) {
                               //Handle error here...
                               if (result instanceof HttpException) {
                                   //HttpException type com.sap.cloud.mobile.foundation.networking.HttpException
                                   HttpException ne = (HttpException) result;
                                   Log.e("Http Exception: ", ne.message() + ", with error code: " + ne.code());
                               } else {
                                   Log.e("Exception occurred: ", result.getMessage());
                               }
                           }
                       }
            );


        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        SAPWizardApplication application = (SAPWizardApplication) getApplication();
      //  startEntitySetListActivity();




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ONBOARDING) {
            if (resultCode == RESULT_OK) {
                secureStorage.setOnboardingCompleted(true);
                Intent intent = new Intent(this, TraceReportActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                finish();
            }
        }
    }

}