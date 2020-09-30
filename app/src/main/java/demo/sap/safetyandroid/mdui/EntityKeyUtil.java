package demo.sap.safetyandroid.mdui;

import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.DataValueList;
import com.sap.cloud.mobile.odata.EntityValue;

public class EntityKeyUtil {
    /**
     * Check entity key and if set returns it in the format of key:value,...
     * EntityKey.toString() return in string format: {"key":value,"key2":value2}
     * @param entityValue containing the entity key
     * @return entity key as string in the format key:value, key:value, ... OR empty string
     */
    public static String getOptionalEntityKey(EntityValue entityValue) {
        String keyString = entityValue.getEntityKey().toString().replace("\"", "").replace(",", ", ");
        return keyString.substring(1, keyString.length()-1);
    }
}