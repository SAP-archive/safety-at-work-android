package demo.sap.safetyandroid.mdui;
import demo.sap.safetyandroid.app.SAPWizardApplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import demo.sap.safetyandroid.mdui.deviceset.DeviceSetActivity;
import demo.sap.safetyandroid.mdui.devicetagset.DeviceTagSetActivity;
import demo.sap.safetyandroid.mdui.deviceuserviewset.DeviceUserViewSetActivity;
import demo.sap.safetyandroid.mdui.ephemeralidinfectedset.EphemeralIDInfectedSetActivity;
import demo.sap.safetyandroid.mdui.ephemeralidset.EphemeralIDSetActivity;
import demo.sap.safetyandroid.mdui.eventset.EventSetActivity;
import demo.sap.safetyandroid.mdui.historydevicesstatus.HistoryDevicesStatusActivity;
import demo.sap.safetyandroid.mdui.historydevicesstatusparameters.HistoryDevicesStatusParametersActivity;
import demo.sap.safetyandroid.mdui.infectedset.InfectedSetActivity;
import demo.sap.safetyandroid.mdui.proximitydetectedset.ProximityDetectedSetActivity;
import demo.sap.safetyandroid.mdui.realtimeroomstatus.RealTimeRoomStatusActivity;
import demo.sap.safetyandroid.mdui.realtimeroomstatusparameters.RealTimeRoomStatusParametersActivity;
import demo.sap.safetyandroid.mdui.tagset.TagSetActivity;
import com.sap.cloud.mobile.fiori.object.ObjectCell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import demo.sap.safetyandroid.R;

/*
 * An activity to display the list of all entity types from the OData service
 */
public class EntitySetListActivity extends AppCompatActivity {

    private static final int SETTINGS_SCREEN_ITEM = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetListActivity.class);
    private static final int BLUE_ANDROID_ICON = R.drawable.ic_android_blue;
    private static final int WHITE_ANDROID_ICON = R.drawable.ic_android_white;

    public enum EntitySetName {
        DeviceSet("DeviceSet", R.string.eset_deviceset,BLUE_ANDROID_ICON),
        DeviceTagSet("DeviceTagSet", R.string.eset_devicetagset,WHITE_ANDROID_ICON),
        DeviceUserViewSet("DeviceUserViewSet", R.string.eset_deviceuserviewset,BLUE_ANDROID_ICON),
        EphemeralIDInfectedSet("EphemeralIDInfectedSet", R.string.eset_ephemeralidinfectedset,WHITE_ANDROID_ICON),
        EphemeralIDSet("EphemeralIDSet", R.string.eset_ephemeralidset,BLUE_ANDROID_ICON),
        EventSet("EventSet", R.string.eset_eventset,WHITE_ANDROID_ICON),
        HistoryDevicesStatus("HistoryDevicesStatus", R.string.eset_historydevicesstatus,BLUE_ANDROID_ICON),
        HistoryDevicesStatusParameters("HistoryDevicesStatusParameters", R.string.eset_historydevicesstatusparameters,WHITE_ANDROID_ICON),
        InfectedSet("InfectedSet", R.string.eset_infectedset,BLUE_ANDROID_ICON),
        ProximityDetectedSet("ProximityDetectedSet", R.string.eset_proximitydetectedset,WHITE_ANDROID_ICON),
        RealTimeRoomStatus("RealTimeRoomStatus", R.string.eset_realtimeroomstatus,BLUE_ANDROID_ICON),
        RealTimeRoomStatusParameters("RealTimeRoomStatusParameters", R.string.eset_realtimeroomstatusparameters,WHITE_ANDROID_ICON),
        TagSet("TagSet", R.string.eset_tagset,BLUE_ANDROID_ICON);

        private int titleId;
        private int iconId;
        private String entitySetName;

        EntitySetName(String name, int titleId, int iconId) {
            this.entitySetName = name;
            this.titleId = titleId;
            this.iconId = iconId;
        }

        public int getTitleId() {
                return this.titleId;
        }

        public String getEntitySetName() {
                return this.entitySetName;
        }
    }

    private final List<String> entitySetNames = new ArrayList<>();
    private final Map<String, EntitySetName> entitySetNameMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entity_set_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        entitySetNames.clear();
        entitySetNameMap.clear();
        for (EntitySetName entitySet : EntitySetName.values()) {
            String entitySetTitle = getResources().getString(entitySet.getTitleId());
            entitySetNames.add(entitySetTitle);
            entitySetNameMap.put(entitySetTitle, entitySet);
        }

        final ListView listView = findViewById(R.id.entity_list);
        final EntitySetListAdapter adapter = new EntitySetListAdapter(this, R.layout.element_entity_set_list, entitySetNames);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            EntitySetName entitySetName = entitySetNameMap.get(adapter.getItem(position));
            Context context = EntitySetListActivity.this;
            Intent intent;
            switch (entitySetName) {
                case DeviceSet:
                    intent = new Intent(context, DeviceSetActivity.class);
                    break;
                case DeviceTagSet:
                    intent = new Intent(context, DeviceTagSetActivity.class);
                    break;
                case DeviceUserViewSet:
                    intent = new Intent(context, DeviceUserViewSetActivity.class);
                    break;
                case EphemeralIDInfectedSet:
                    intent = new Intent(context, EphemeralIDInfectedSetActivity.class);
                    break;
                case EphemeralIDSet:
                    intent = new Intent(context, EphemeralIDSetActivity.class);
                    break;
                case EventSet:
                    intent = new Intent(context, EventSetActivity.class);
                    break;
                case HistoryDevicesStatus:
                    intent = new Intent(context, HistoryDevicesStatusActivity.class);
                    break;
                case HistoryDevicesStatusParameters:
                    intent = new Intent(context, HistoryDevicesStatusParametersActivity.class);
                    break;
                case InfectedSet:
                    intent = new Intent(context, InfectedSetActivity.class);
                    break;
                case ProximityDetectedSet:
                    intent = new Intent(context, ProximityDetectedSetActivity.class);
                    break;
                case RealTimeRoomStatus:
                    intent = new Intent(context, RealTimeRoomStatusActivity.class);
                    break;
                case RealTimeRoomStatusParameters:
                    intent = new Intent(context, RealTimeRoomStatusParametersActivity.class);
                    break;
                case TagSet:
                    intent = new Intent(context, TagSetActivity.class);
                    break;
                    default:
                        return;
            }
            context.startActivity(intent);
        });
    }

    public class EntitySetListAdapter extends ArrayAdapter<String> {

        EntitySetListAdapter(@NonNull Context context, int resource, List<String> entitySetNames) {
            super(context, resource, entitySetNames);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            EntitySetName entitySetName = entitySetNameMap.get(getItem(position));
            if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.element_entity_set_list, parent, false);
            }
            String headLineName = getResources().getString(entitySetName.titleId);
            ObjectCell entitySetCell = convertView.findViewById(R.id.entity_set_name);
            entitySetCell.setHeadline(headLineName);
            entitySetCell.setDetailImage(entitySetName.iconId);
            return convertView;
        }
    }
                
    @Override
    public void onBackPressed() {
            moveTaskToBack(true);
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SETTINGS_SCREEN_ITEM, 0, R.string.menu_item_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOGGER.debug("onOptionsItemSelected: " + item.getTitle());
        switch (item.getItemId()) {
            case SETTINGS_SCREEN_ITEM:
                LOGGER.debug("settings screen menu item selected.");
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivityForResult(intent, SETTINGS_SCREEN_ITEM);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGGER.debug("EntitySetListActivity::onActivityResult, request code: " + requestCode + " result code: " + resultCode);
        if (requestCode == SETTINGS_SCREEN_ITEM) {
            LOGGER.debug("Calling AppState to retrieve settings after settings screen is closed.");
        }
    }

}
