package com.example.mygcs;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.o3dr.android.client.interfaces.DroneListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DroneListener {

    NaverMap myMap;
    private boolean click1 = true;
    private boolean click2 = true;
    private boolean click3 = false;
    private boolean click5 = true;
    public void deleteMapMenuVisibility(){
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

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

        final Button btn1 = (Button) findViewById(R.id.satellite);
        final Button btn2 = (Button) findViewById(R.id.terrain);
        final Button btn3 = (Button) findViewById(R.id.hybrid);
        final Button btn4 = (Button) findViewById(R.id.menu);
        final Button btn5 = (Button) findViewById(R.id.cadastral_off);
        final Button btn6 = (Button) findViewById(R.id.cadastral_menu);
        final Button btn7 = (Button) findViewById(R.id.map_move);
        final Button btn8 = (Button) findViewById(R.id.map_lock);
        final Button btn9 = (Button) findViewById(R.id.map_lock_menu);

        myMap.setMapType(NaverMap.MapType.Hybrid);
        btn3.setBackgroundColor(Color.YELLOW);

        deleteMapMenuVisibility();
        deleteCadastralMenuVisibility();
        deleteMapLockMenuVisibility();

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
            boolean click = true;

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
                if (click) {
                    btn4.setBackgroundColor(Color.YELLOW);
                    click = false;
                } else {
                    btn4.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    click = true;
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
            boolean click = true;

            @Override
            public void onClick(View view) {

                if (btn5.getVisibility() == view.GONE) {
                    btn5.setVisibility(view.VISIBLE);
                } else {
                    btn5.setVisibility(view.GONE);
                }
                if (click) {
                    btn6.setBackgroundColor(Color.YELLOW);
                    click = false;
                } else {
                    btn6.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    click = true;
                }
            }
        });

        btn9.setOnClickListener(new Button.OnClickListener() {
            boolean click = true;

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
                if (click) {
                    btn9.setBackgroundColor(Color.YELLOW);
                    click = false;
                } else {
                    btn9.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    click = true;
                }
            }
        });

    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }
}
