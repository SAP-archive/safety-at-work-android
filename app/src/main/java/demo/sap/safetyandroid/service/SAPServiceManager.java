package demo.sap.safetyandroid.service;

import com.sap.cloud.android.odata.v2.v2;
import com.sap.cloud.mobile.flowv2.model.AppConfig;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.odata.OnlineODataProvider;
import com.sap.cloud.mobile.odata.core.Action0;
import com.sap.cloud.mobile.odata.http.OKHttpHandler;

public class SAPServiceManager {

    private final AppConfig appConfig;
    private OnlineODataProvider provider;
    private String serviceRoot;
    v2 v2;
    public static final String CONNECTION_ID_V2 = "safetyatworkdestination";

    public SAPServiceManager(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void openODataStore(Action0 callback) {
        if (appConfig != null) {
            String serviceUrl = appConfig.getServiceUrl();
            provider = new OnlineODataProvider("SAPService", serviceUrl + CONNECTION_ID_V2);
            provider.getNetworkOptions().setHttpHandler(new OKHttpHandler(ClientProvider.get()));
            provider.getServiceOptions().setCheckVersion(false);
            provider.getServiceOptions().setRequiresType(true);
            provider.getServiceOptions().setCacheMetadata(false);
            v2 = new v2(provider);

        }
        callback.call();
    }

    public String getServiceRoot() {
        if (serviceRoot == null) {
            if (v2 == null) {
                throw new IllegalStateException("SAPServiceManager was not initialized");
            }
            provider = (OnlineODataProvider)v2.getProvider();
            serviceRoot = provider.getServiceRoot();


        }
        return serviceRoot;
    }

    // This getter is used for the master-detail ui generation
    public v2 getv2() {
        if (v2 == null) {
            throw new IllegalStateException("SAPServiceManager was not initialized");
        }
        return v2;
    }

}