package com.example.mygcs;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationSource;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.companion.solo.SoloState;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.AlertDialog.*;
import static com.o3dr.services.android.lib.drone.property.VehicleMode.COPTER_GUIDED;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DroneListener, TowerListener, LinkListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    NaverMap myMap;
    protected Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private final Handler handler = new Handler();
    Marker marker;
    Marker marker2 = new Marker();
    private Spinner modeSelector;
    private double altitudeSetting = 0.0;
    private double flightRangeSetting = 0.0;
    private double AbDistanceSetting = 0.0;
    private boolean click1 = true;
    private boolean click2 = true;
    private boolean click3 = false;
    private boolean click5 = true;
    private boolean click6 = true;
    ArrayList<LatLng> pointList = new ArrayList<>();
    private boolean menu_click1 = true;
    private boolean menu_click2 = true;
    private boolean menu_click3 = true;
    private boolean menu_click4 = true;
    private boolean ab_click = true;
    private boolean distance_click = true;
    private boolean mission_click = true;
    GuideMode guideMode;
    //PolygonSpray polygonSpray;

    public MainActivity(){
        marker = new Marker();
    }

    public void deleteAllMenuVisibility(){
        deleteMapMenuVisibility();
        deleteMapTypeMenuVisibility();
        deleteCadastralMenuVisibility();
        deleteMapLockMenuVisibility();
        deleteAltitudeMenuVisibility();
    }
    public void onFlightModeSelected(View view) {
        VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();

        VehicleApi.getApi(this.drone).setVehicleMode(vehicleMode, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Vehicle mode change successful.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Vehicle mode change failed: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Vehicle mode change timed out.");
            }
        });
    }

    public void deleteAltitudeMenuVisibility(){
        View view = null;
        Button alt_set = (Button) findViewById(R.id.altitude_setting);
        Button inc_alt = (Button) findViewById(R.id.increase_altitude);
        Button dec_alt = (Button) findViewById(R.id.decrease_altitude);
        alt_set.setVisibility(view.GONE);
        inc_alt.setVisibility(view.GONE);
        dec_alt.setVisibility(view.GONE);
    }


    public void deleteMapMenuVisibility(){
        View view = null;
        Button flight_btn = (Button) findViewById(R.id.flight_btn);
        Button map_lock = (Button) findViewById(R.id.map_lock_menu);
        Button menu = (Button) findViewById(R.id.map_menu);
        Button cadastral_menu = (Button) findViewById(R.id.cadastral_menu);
        Button clear = (Button) findViewById(R.id.clear);
        ImageButton disconBtn = (ImageButton) findViewById(R.id.disconnBtn);

        flight_btn.setVisibility(view.GONE);
        map_lock.setVisibility(view.GONE);
        menu.setVisibility(view.GONE);
        cadastral_menu.setVisibility(view.GONE);
        clear.setVisibility(view.GONE);
        disconBtn.setVisibility(view.GONE);
    }
    public void deleteMapTypeMenuVisibility(){
        View view = null;
        Button satellite = (Button) findViewById(R.id.satellite);
        Button terrain = (Button) findViewById(R.id.terrain);
        Button hybrid = (Button) findViewById(R.id.hybrid);

        satellite.setVisibility(view.GONE);
        terrain.setVisibility(view.GONE);
        hybrid.setVisibility(view.GONE);
    }

    public void deleteCadastralMenuVisibility(){
        View view = null;
        Button btn1 = (Button) findViewById(R.id.cadastral_off);

        btn1.setVisibility(view.GONE);
    }

    public void deleteMapLockMenuVisibility(){
        View view = null;
        Button btn1 = (Button) findViewById(R.id.map_move);
        Button btn2 = (Button) findViewById(R.id.map_lock);

        btn1.setVisibility(view.GONE);
        btn2.setVisibility(view.GONE);
    }
    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }
    private void checkSoloState() {
        final SoloState soloState = drone.getAttribute(SoloAttributes.SOLO_STATE);
        if (soloState == null){
            alertUser("Unable to retrieve the solo state.");
        }
        else {
            alertUser("Solo state is up to date.");
        }
    }
    protected void updateAltitude() {
        TextView altitudeTextView = (TextView) findViewById(R.id.altitude_number);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
    }

    protected void updateSpeed() {
        TextView speedTextView = (TextView) findViewById(R.id.velocity_number);
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
    }
    protected void updateBattery() {
        TextView altitudeTextView = (TextView) findViewById(R.id.voltage_number);
        Battery droneBattery = this.drone.getAttribute(AttributeType.BATTERY);
        altitudeTextView.setText(String.format("%3.1f", droneBattery.getBatteryVoltage()) + "V");
    }
    protected void updateYAW() {
        TextView attitudeTextView = (TextView) findViewById(R.id.YAW_number);
        Attitude droneAttitude = this.drone.getAttribute(AttributeType.ATTITUDE);
        attitudeTextView.setText(String.format("%3.1f", droneAttitude.getYaw()) + "deg");

        float droneAngle = (float)droneAttitude.getYaw();
        if(droneAngle < 0){droneAngle += 360;}
        marker.setAngle(droneAngle);

    }
    protected void updateNumberOfSatellite() {
        TextView numberOfSatellitesTextView = (TextView) findViewById(R.id.satellite_number);
        Gps droneNumberOfSatellites = this.drone.getAttribute(AttributeType.GPS);
        Log.d("MYLOG", "위성 수 변화 : " + droneNumberOfSatellites.getSatellitesCount());
        numberOfSatellitesTextView.setText(String.format("%3d", droneNumberOfSatellites.getSatellitesCount()));
    }
    protected void updateGPS(){
        Gps droneLocation = this.drone.getAttribute(AttributeType.GPS);
        marker.setPosition(new LatLng(droneLocation.getPosition().getLatitude(), droneLocation.getPosition().getLongitude()));
        marker.setMap(myMap);
        marker.setIcon(OverlayImage.fromResource(R.drawable.gcsmarker));
        marker.setAnchor(new PointF((float)0.5, (float)0.77));
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(droneLocation.getPosition().getLatitude(), droneLocation.getPosition().getLongitude()));
        myMap.moveCamera(cameraUpdate);
        //
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();
        GuidedState guidedState = drone.getAttribute(AttributeType.GUIDED_STATE);
        LatLng target = new LatLng(guidedState.getCoordinate().getLatitude(),
                guidedState.getCoordinate().getLongitude());


        if(vehicleState.isFlying()&&vehicleMode == VehicleMode.COPTER_GUIDED && target != null) {
            if(guideMode.CheckGoal(this.drone, new LatLng(droneLocation.getPosition().getLatitude(), droneLocation.getPosition().getLongitude()))){
                alertUser("Reached Goal");
            }
        }
    }
    protected void updateVehicleMode() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();
        ArrayAdapter arrayAdapter = (ArrayAdapter) this.modeSelector.getAdapter();
        this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
    }
    protected void updateArmButton(){
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button armButton = (Button) findViewById(R.id.flight_btn);


        if (vehicleState.isFlying() && this.drone.isConnected()) {
            // Land
            armButton.setText("LAND");
        } else if (vehicleState.isArmed()) {
            // Take off
            armButton.setText("TAKE OFF");
        } else if (vehicleState.isConnected()) {
            // Connected but not Armed
            armButton.setText("ARM");
        }

    }
    public void showArmDialogue(){
        Builder alert = new Builder(this);
        alert.setTitle("경고");
        alert.setMessage("모터를 가동합니다.\n모터가 고속으로 회전합니다.");

        alert.setPositiveButton("확인", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VehicleApi.getApi(drone).arm(true, false, new SimpleCommandListener() {
                    @Override
                    public void onError(int executionError) {
                        alertUser("Unable to arm vehicle.");
                    }

                    @Override
                    public void onTimeout() {
                        alertUser("Arming operation timed out.");
                    }
                });
            }
        });

        alert.setNegativeButton("취소", new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });

        alert.show();
    }
    public void showTakeOffDialogue(){
        Builder alert = new Builder(this);
        alert.setTitle("경고");
        alert.setMessage("지정한 이륙 고도까지 기체가 상승합니다.\n안전거리를 유지하세요.");

        alert.setPositiveButton("확인", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ControlApi.getApi(drone).takeoff(altitudeSetting, new AbstractCommandListener() {

                    @Override
                    public void onSuccess() {
                        alertUser("Taking off...");
                    }

                    @Override
                    public void onError(int i) {
                        alertUser("Unable to take off.");
                    }

                    @Override
                    public void onTimeout() {
                        alertUser("Unable to take off.");
                    }
                });
            }
        });

        alert.setNegativeButton("취소", new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });

        alert.show();
    }
    protected void checkConnection(){
        if(this.drone.isConnected()){
            this.drone.disconnect();
        }
    }
    protected void updateVehicleModesForType(int droneType) {

        List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(droneType);
        ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
        vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.modeSelector.setAdapter(vehicleModeArrayAdapter);
    }
    protected void connectDrone(){
        ConnectionParameter connectionParams =ConnectionParameter.newUdpConnection(null);
        this.drone.connect(connectionParams);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);
        this.modeSelector = (Spinner) findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
                ((TextView)modeSelector.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        this.guideMode = new GuideMode();
        //this.polygonSpray = new PolygonSpray(MainActivity.this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.myMap = naverMap;
        connectDrone();
        marker.setIcon(OverlayImage.fromResource(R.drawable.gcsmarker));
        final Button satellite_btn = (Button) findViewById(R.id.satellite);
        final Button terrain_btn = (Button) findViewById(R.id.terrain);
        final Button hybrid_btn = (Button) findViewById(R.id.hybrid);
        final Button map_menu = (Button) findViewById(R.id.map_menu);
        final Button cadastral_off = (Button) findViewById(R.id.cadastral_off);
        final Button cadastral_menu = (Button) findViewById(R.id.cadastral_menu);
        final Button map_move = (Button) findViewById(R.id.map_move);
        final Button map_lock = (Button) findViewById(R.id.map_lock);
        final Button map_lock_menu = (Button) findViewById(R.id.map_lock_menu);

        final Button clear_btn = (Button) findViewById(R.id.clear);
        final Button flight_btn = (Button) findViewById(R.id.flight_btn);
        final ImageButton disBtn = (ImageButton) findViewById(R.id.disconnBtn);
        final Button altBtn = (Button) findViewById(R.id.altitude_setting);
        final Button inAltBtn = (Button) findViewById(R.id.increase_altitude);
        final Button deAltBtn = (Button) findViewById(R.id.decrease_altitude);

        myMap.setMapType(NaverMap.MapType.Hybrid);
        hybrid_btn.setBackgroundColor(Color.YELLOW);

        deleteAllMenuVisibility();


        satellite_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click1) {
                    satellite_btn.setBackgroundColor(Color.YELLOW);
                    click1 = false;
                    click2 = true;
                    click3 = true;
                    terrain_btn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    hybrid_btn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    myMap.setMapType(NaverMap.MapType.Satellite);
                    map_menu.setText("위성지도");
                }
            }
        });

        terrain_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click2) {
                    terrain_btn.setBackgroundColor(Color.YELLOW);
                    click1 = true;
                    click2 = false;
                    click3 = true;
                    satellite_btn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    hybrid_btn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    myMap.setMapType(NaverMap.MapType.Terrain);
                    map_menu.setText("지형도");
                }
            }
        });

        hybrid_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click3) {
                    hybrid_btn.setBackgroundColor(Color.YELLOW);
                    click1 = true;
                    click2 = true;
                    click3 = false;
                    satellite_btn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    terrain_btn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    myMap.setMapType(NaverMap.MapType.Hybrid);
                    map_menu.setText("일반지도");
                }
            }
        });

        map_menu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (satellite_btn.getVisibility() == view.GONE) {
                    satellite_btn.setVisibility(view.VISIBLE);
                } else {
                    satellite_btn.setVisibility(view.GONE);
                }
                if (terrain_btn.getVisibility() == view.GONE) {
                    terrain_btn.setVisibility(view.VISIBLE);
                } else {
                    terrain_btn.setVisibility(view.GONE);
                }
                if (hybrid_btn.getVisibility() == view.GONE) {
                    hybrid_btn.setVisibility(view.VISIBLE);
                } else {
                    hybrid_btn.setVisibility(view.GONE);
                }
                if (menu_click1) {
                    map_menu.setBackgroundColor(Color.YELLOW);
                    menu_click1 = false;
                } else {
                    map_menu.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click1 = true;
                }
            }

        });

        cadastral_off.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click5) {
                    cadastral_off.setBackgroundColor(Color.YELLOW);
                    click5 = false;
                    myMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
                    cadastral_off.setText("ON");
                }
                else{
                    cadastral_off.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    click5 = true;
                    myMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
                    cadastral_off.setText("OFF");
                }
            }
        });

        cadastral_menu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cadastral_off.getVisibility() == view.GONE) {
                    cadastral_off.setVisibility(view.VISIBLE);
                } else {
                    cadastral_off.setVisibility(view.GONE);
                }
                if (menu_click2) {
                    cadastral_menu.setBackgroundColor(Color.YELLOW);
                    menu_click2 = false;
                } else {
                    cadastral_menu.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click2 = true;
                }
            }
        });

        map_lock_menu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (map_move.getVisibility() == view.GONE) {
                    map_move.setVisibility(view.VISIBLE);
                } else {
                    map_move.setVisibility(view.GONE);
                }
                if (map_lock.getVisibility() == view.GONE) {
                    map_lock.setVisibility(view.VISIBLE);
                } else {
                    map_lock.setVisibility(view.GONE);
                }
                if (menu_click3) {
                    map_lock_menu.setBackgroundColor(Color.YELLOW);
                    menu_click3 = false;
                } else {
                    map_lock_menu.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click3 = true;
                }
            }
        });
        flight_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                State vehicleState = drone.getAttribute(AttributeType.STATE);

                if (vehicleState.isFlying()) {
                    // Land
                    VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_LAND, new SimpleCommandListener() {
                        @Override
                        public void onError(int executionError) {
                            alertUser("Unable to land the vehicle.");
                        }

                        @Override
                        public void onTimeout() {
                            alertUser("Unable to land the vehicle.");
                        }
                    });
                } else if (vehicleState.isArmed()) {
                    // Take off
                    showTakeOffDialogue();
                } else if (!vehicleState.isConnected()) {
                    // Connect
                    alertUser("Connect to a drone first");
                } else {
                    // Connected but not Armed

                    showArmDialogue();
                }
            }
        });
        disBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });
        altBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inAltBtn.getVisibility() == view.GONE) {
                    inAltBtn.setVisibility(view.VISIBLE);
                } else {
                    inAltBtn.setVisibility(view.GONE);
                }
                if (deAltBtn.getVisibility() == view.GONE) {
                    deAltBtn.setVisibility(view.VISIBLE);
                } else {
                    deAltBtn.setVisibility(view.GONE);
                }
                if (menu_click4) {
                    altBtn.setBackgroundColor(Color.YELLOW);
                    menu_click4 = false;
                } else {
                    altBtn.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click4 = true;
                }
            }
        });
        inAltBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                altitudeSetting += 0.5;
                altBtn.setText(String.format("이륙고도\n%3.1f", altitudeSetting) + "m");
            }
        });
        deAltBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(altitudeSetting >= 0.5){
                    altitudeSetting -= 0.5;
                    altBtn.setText(String.format("이륙고도\n%3.1f", altitudeSetting) + "m");
                }
            }
        });
        //

        //
        myMap.setOnMapLongClickListener(new NaverMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                State vehicleState = drone.getAttribute(AttributeType.STATE);
                if(vehicleState.isFlying()) {
                    guideMode.mMarkerGuide.setPosition(latLng);
                    guideMode.mMarkerGuide.setMap(myMap);
                    guideMode.mGuidedPoint = latLng;
                    guideMode.DialogSimple(drone, new LatLong(latLng.latitude,latLng.longitude));
                }else{
                    if(!vehicleState.isConnected()){
                        alertUser("Drone Connection Failed");
                    }
                    if(!vehicleState.isFlying()){
                        alertUser("Drone is not flying");
                    }
                }


            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        }

        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }
    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
                checkSoloState();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Disconnected");
                break;

            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                updateArmButton();
                break;

            case AttributeEvent.TYPE_UPDATED:
                Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                    updateVehicleModesForType(this.droneType);
                }
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
                updateVehicleMode();
                break;
            case AttributeEvent.BATTERY_UPDATED:
                updateBattery();
                break;
            case AttributeEvent.SPEED_UPDATED:
                updateSpeed();
                break;

            case AttributeEvent.ALTITUDE_UPDATED:
                updateAltitude();
                break;
            case AttributeEvent.ATTITUDE_UPDATED:
                updateYAW();
                break;
            case AttributeEvent.GPS_COUNT:
                updateNumberOfSatellite();
                break;
            case AttributeEvent.GPS_POSITION:
                updateGPS();
                break;
            default:
                // Log.i("DRONE_EVENT", event); //Uncomment to see events from the drone
                break;
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    @Override
    public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
        switch(connectionStatus.getStatusCode()){
            case LinkConnectionStatus.FAILED:
                Bundle extras = connectionStatus.getExtras();
                String msg = null;
                if (extras != null) {
                    msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                }
                alertUser("Connection Failed:" + msg);
                break;
        }
    }

    @Override
    public void onTowerConnected() {
        alertUser("DroneKit-Android Connected");
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {
        alertUser("DroneKit-Android Interrupted");
    }


    class GuideMode {
        LatLng mGuidedPoint; //가이드모드 목적지 저장
        Marker mMarkerGuide = new com.naver.maps.map.overlay.Marker(); //GCS 위치 표시 마커 옵션
        OverlayImage guideIcon = OverlayImage.fromResource(R.drawable.gcsmarker);
        void DialogSimple(final Drone drone, final LatLong point) {
            State vehicleState = drone.getAttribute(AttributeType.STATE);
            VehicleMode vehicleMode = vehicleState.getVehicleMode();

            //this.mGuidedPoint = new LatLng(point.getLatitude(), point.getLongitude());

            if(vehicleMode != VehicleMode.COPTER_GUIDED) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                alt_bld.setMessage("확인하시면 가이드모드로 전환후 기체가 이동합니다.").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        VehicleApi.getApi(drone).setVehicleMode(COPTER_GUIDED,
                                new AbstractCommandListener() {
                                    @Override

                                    public void onSuccess() {
                                        ControlApi.getApi(drone).goTo(point, true, null);
                                    }

                                    @Override
                                    public void onError(int i) {

                                    }

                                    @Override
                                    public void onTimeout() {
                                    }
                                });
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = alt_bld.create();
                // Title for AlertDialog
                alert.setTitle("Title");
                // Icon for AlertDialog
                alert.setIcon(R.drawable.drone);
                alert.show();
            }
            else if(vehicleMode == VehicleMode.COPTER_GUIDED){
                //this.mGuidedPoint = new LatLng(point.getLatitude(), point.getLongitude());
                ControlApi.getApi(drone).goTo(point, true, null);
            }
        }

        public boolean CheckGoal(final Drone drone, LatLng recentLatLng) {
            GuidedState guidedState = drone.getAttribute(AttributeType.GUIDED_STATE);
            LatLng target = new LatLng(guidedState.getCoordinate().getLatitude(),
                    guidedState.getCoordinate().getLongitude());
            Log.d("myLog", "목표 : "+ target + " 드론 : " + recentLatLng + " 거리: "+ target.distanceTo(recentLatLng));
            return target.distanceTo(recentLatLng) <= 1;
        }
    }


}
