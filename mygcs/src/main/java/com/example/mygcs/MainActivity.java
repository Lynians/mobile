package com.example.mygcs;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ImageButton;
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
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DroneListener, TowerListener, LinkListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    NaverMap myMap;
    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private final Handler handler = new Handler();
    Marker marker;
    Marker marker2 = new Marker();

    private boolean click1 = true;
    private boolean click2 = true;
    private boolean click3 = false;
    private boolean click5 = true;

    private boolean menu_click1 = true;
    private boolean menu_click2 = true;
    private boolean menu_click3 = true;
    public MainActivity(){
        marker = new Marker();
    }
    public void deleteMapMenuVisibility(){
        View view = null;
        Button btn1 = (Button) findViewById(R.id.flight_btn);
        Button btn2 = (Button) findViewById(R.id.map_lock_menu);
        Button btn3 = (Button) findViewById(R.id.menu);
        Button btn4 = (Button) findViewById(R.id.cadastral_menu);
        Button btn5 = (Button) findViewById(R.id.clear);

        btn1.setVisibility(view.GONE);
        btn2.setVisibility(view.GONE);
        btn3.setVisibility(view.GONE);
        btn4.setVisibility(view.GONE);
        btn5.setVisibility(view.GONE);
    }
    public void deleteMapTypeMenuVisibility(){
        View view = null;
        Button btn1 = (Button) findViewById(R.id.satellite);
        Button btn2 = (Button) findViewById(R.id.terrain);
        Button btn3 = (Button) findViewById(R.id.hybrid);

        btn1.setVisibility(view.GONE);
        btn2.setVisibility(view.GONE);
        btn3.setVisibility(view.GONE);
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
        //marker.setMap(null);
        float droneAngle = (float)droneAttitude.getYaw();
        if(droneAngle < 0){droneAngle += 360;}
        marker.setAngle(droneAngle);
        //marker.setMap(myMap);
        //marker.setIcon(OverlayImage.fromResource(R.drawable.gcsmarker));
    }
    protected void updateNumberOfSatellite() {
        TextView numberOfSatellitesTextView = (TextView) findViewById(R.id.satellite_number);
        Gps droneNumberOfSatellites = this.drone.getAttribute(AttributeType.GPS);
        Log.d("MYLOG", "위성 수 변화 : " + droneNumberOfSatellites.getSatellitesCount());
        numberOfSatellitesTextView.setText(String.format("%3d", droneNumberOfSatellites.getSatellitesCount()));
    }
    protected void updateGPS(){
        Gps droneLocation = this.drone.getAttribute(AttributeType.GPS);
        //marker.setMap(null);
        marker.setPosition(new LatLng(droneLocation.getPosition().getLatitude(), droneLocation.getPosition().getLongitude()));
        marker.setMap(myMap);
        marker.setIcon(OverlayImage.fromResource(R.drawable.gcsmarker));
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(droneLocation.getPosition().getLatitude(), droneLocation.getPosition().getLongitude()));
        myMap.moveCamera(cameraUpdate);
        //marker2.setMap(null);
       // marker2.setPosition(new LatLng(droneLocation.getPosition().getLatitude(), droneLocation.getPosition().getLongitude()));
        //marker2.setMap(myMap);


    }
    protected void updateArmButton(){
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button armButton = (Button) findViewById(R.id.flight_btn);


        if (vehicleState.isFlying() && !this.drone.isConnected()) {
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

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.myMap = naverMap;
        connectDrone();
        marker.setIcon(OverlayImage.fromResource(R.drawable.gcsmarker));
        final Button btn1 = (Button) findViewById(R.id.satellite);
        final Button btn2 = (Button) findViewById(R.id.terrain);
        final Button btn3 = (Button) findViewById(R.id.hybrid);
        final Button btn4 = (Button) findViewById(R.id.menu);
        final Button btn5 = (Button) findViewById(R.id.cadastral_off);
        final Button btn6 = (Button) findViewById(R.id.cadastral_menu);
        final Button btn7 = (Button) findViewById(R.id.map_move);
        final Button btn8 = (Button) findViewById(R.id.map_lock);
        final Button btn9 = (Button) findViewById(R.id.map_lock_menu);
        final ImageButton menu_btn = (ImageButton) findViewById(R.id.menu_btn);
        final Button btn10 = (Button) findViewById(R.id.clear);
        final Button btn11 = (Button) findViewById(R.id.flight_btn);

        myMap.setMapType(NaverMap.MapType.Hybrid);
        btn3.setBackgroundColor(Color.YELLOW);

        deleteMapMenuVisibility();
        deleteMapTypeMenuVisibility();
        deleteCadastralMenuVisibility();
        deleteMapLockMenuVisibility();

        menu_btn.setOnClickListener(new Button.OnClickListener() {
            boolean click = true;

            @Override
            public void onClick(View view) {
                if(click){
                    btn4.setVisibility(view.VISIBLE);
                    btn6.setVisibility(view.VISIBLE);
                    btn9.setVisibility(view.VISIBLE);
                    btn10.setVisibility(view.VISIBLE);
                    btn11.setVisibility(view.VISIBLE);
                    click = false;
                }
                else{
                    btn1.setVisibility(view.GONE);
                    btn2.setVisibility(view.GONE);
                    btn3.setVisibility(view.GONE);
                    btn4.setVisibility(view.GONE);
                    btn4.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    btn5.setVisibility(view.GONE);
                    btn6.setVisibility(view.GONE);
                    btn6.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    btn7.setVisibility(view.GONE);
                    btn8.setVisibility(view.GONE);
                    btn9.setVisibility(view.GONE);
                    btn9.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    btn10.setVisibility(view.GONE);
                    btn11.setVisibility(view.GONE);
                    menu_click1 = true;
                    menu_click2 = true;
                    menu_click3 = true;
                    click = true;
                }
            }

        });
        btn1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click1) {
                    btn1.setBackgroundColor(Color.YELLOW);
                    click1 = false;
                    click2 = true;
                    click3 = true;
                    btn2.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    btn3.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    myMap.setMapType(NaverMap.MapType.Satellite);
                    btn4.setText("위성지도");
                }
            }
        });

        btn2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click2) {
                    btn2.setBackgroundColor(Color.YELLOW);
                    click1 = true;
                    click2 = false;
                    click3 = true;
                    btn1.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    btn3.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    myMap.setMapType(NaverMap.MapType.Terrain);
                    btn4.setText("지형도");
                }
            }
        });

        btn3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click3) {
                    btn3.setBackgroundColor(Color.YELLOW);
                    click1 = true;
                    click2 = true;
                    click3 = false;
                    btn1.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    btn2.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    myMap.setMapType(NaverMap.MapType.Hybrid);
                    btn4.setText("일반지도");
                }
            }
        });

        btn4.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btn1.getVisibility() == view.GONE) {
                    btn1.setVisibility(view.VISIBLE);
                } else {
                    btn1.setVisibility(view.GONE);
                }
                if (btn2.getVisibility() == view.GONE) {
                    btn2.setVisibility(view.VISIBLE);
                } else {
                    btn2.setVisibility(view.GONE);
                }
                if (btn3.getVisibility() == view.GONE) {
                    btn3.setVisibility(view.VISIBLE);
                } else {
                    btn3.setVisibility(view.GONE);
                }
                if (menu_click1) {
                    btn4.setBackgroundColor(Color.YELLOW);
                    menu_click1 = false;
                } else {
                    btn4.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click1 = true;
                }
            }

        });

        btn5.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click5) {
                    btn5.setBackgroundColor(Color.YELLOW);
                    click5 = false;
                    myMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
                    btn5.setText("ON");
                }
                else{
                    btn5.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    click5 = true;
                    myMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
                    btn5.setText("OFF");
                }
            }
        });

        btn6.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btn5.getVisibility() == view.GONE) {
                    btn5.setVisibility(view.VISIBLE);
                } else {
                    btn5.setVisibility(view.GONE);
                }
                if (menu_click2) {
                    btn6.setBackgroundColor(Color.YELLOW);
                    menu_click2 = false;
                } else {
                    btn6.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click2 = true;
                }
            }
        });

        btn9.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btn7.getVisibility() == view.GONE) {
                    btn7.setVisibility(view.VISIBLE);
                } else {
                    btn7.setVisibility(view.GONE);
                }
                if (btn8.getVisibility() == view.GONE) {
                    btn8.setVisibility(view.VISIBLE);
                } else {
                    btn8.setVisibility(view.GONE);
                }
                if (menu_click3) {
                    btn9.setBackgroundColor(Color.YELLOW);
                    menu_click3 = false;
                } else {
                    btn9.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    menu_click3 = true;
                }
            }
        });
        btn11.setOnClickListener(new Button.OnClickListener() {
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
                    ControlApi.getApi(drone).takeoff(10, new AbstractCommandListener() {

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
                } else if (!vehicleState.isConnected()) {
                    // Connect
                    alertUser("Connect to a drone first");
                } else {
                    // Connected but not Armed
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

            //case AttributeEvent.TYPE_UPDATED:
                //Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
              //  if (newDroneType.getDroneType() != this.droneType) {
               //     this.droneType = newDroneType.getDroneType();
              //      updateVehicleModesForType(this.droneType);
             //   }
             //   break;

            //case AttributeEvent.STATE_VEHICLE_MODE:
               // updateVehicleMode();
                //break;
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
}
