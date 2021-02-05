package uteq.student.deber_maps_api;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    private GoogleMap mMap;
    private CheckBox checkBox;
    private SeekBar seekred,seekgreen, seekBblue;
    private Button btdraw,btclesar;
    private Polygon polygon= null;
    private List<LatLng> latLngList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private int red=0, green=0,blue=0;
    private LinearLayout layoutAnimado;
    private LatLng LatMove=null;
    private boolean isDoubleClick=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //asignar variables del layout
        layoutAnimado = (LinearLayout) findViewById(R.id.animado);
        layoutAnimado.setVisibility(View.GONE);
        //asignar variables opciones del poligono
        checkBox = findViewById(R.id.check_box);
        seekred = findViewById(R.id.seek_red);
        seekgreen = findViewById(R.id.seek_green);
        seekBblue = findViewById(R.id.seek_blue);
        btdraw = findViewById(R.id.bt_draw);
        btclesar = findViewById(R.id.bt_clear);
        //creacion del spinner para seleccionar el TIPO de MAPA
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.tipo,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        //asignacion del mapa dentro del SUPERFRAGMENTO
        SupportMapFragment mapFragmnt = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragmnt.getMapAsync(this);

        //Registro devolución de llamada para que se invoque cuando cambie el estado marcado de este CheckBox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if(b){
                    if(polygon==null) return;
                    polygon.setFillColor(Color.rgb(red,green,blue));
                }else {
                    polygon.setFillColor(Color.TRANSPARENT);
                }
            }
        });
        //Habilitar recibir notificaciones de las acciones del usuario de los SeekBar
        seekred.setOnSeekBarChangeListener(this);
        seekgreen.setOnSeekBarChangeListener(this);
        seekBblue.setOnSeekBarChangeListener(this);
    }

    //funcion del boton para borrar el poligono
    public void btnBorrarPoli(View V){
        if(polygon != null) polygon.remove();
        for (Marker marker: markerList) marker.remove();
        latLngList.clear();
        markerList.clear();
        checkBox.setChecked(false);
        seekred.setProgress(0);
        seekBblue.setProgress(0);
        seekgreen.setProgress(0);
    }

    //funcion para el boton crear el poligono
    public void btnCrearPoli(View v){
        if(polygon !=null) polygon.remove();
        PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList)
                .clickable(true);
        polygon = mMap.addPolygon(polygonOptions);
        polygon.setStrokeColor(Color.rgb(red,green,blue));
        if(checkBox.isChecked())
            polygon.setFillColor(Color.rgb(red,green,blue));
    }

    //funcion para mover la camara con un angulo de 70 y 0 grados sobre el Markers
    public void moveCamera(View v){
        if(isDoubleClick==false){
        LatLng ubic =  new LatLng(LatMove.latitude, LatMove.longitude);
            CameraPosition camPos = new CameraPosition.Builder()
                .target(ubic)
                .zoom(19)
                .bearing(45)      //noreste arriba
                .tilt(70)         //punto de vista de la cámara 70 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);
        mMap.animateCamera(camUpd3);
        isDoubleClick=true;
        }else {
            LatLng ubic = new LatLng(LatMove.latitude, LatMove.longitude);
            CameraPosition camPos = new CameraPosition.Builder()
                    .target(ubic)
                    .zoom(19)
                    .bearing(45)
                    .tilt(0)
                    .build();
            CameraUpdate camUpd3 =
                    CameraUpdateFactory.newCameraPosition(camPos);
            mMap.animateCamera(camUpd3);
            isDoubleClick=false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                LatMove = latLng;
                MarkerOptions markerOptions = new MarkerOptions();
                //habilitacion de marcador arrastrable
                markerOptions.position(latLng).title(latLng.latitude +": " +latLng.longitude).draggable(true);
                Marker marker= mMap.addMarker(markerOptions);
                latLngList.add(latLng);
                markerList.add(marker);
                //traducir entre la ubicación en la pantalla y las coordenadas geográficas en la superficie de la Tierra
                Projection  proj = mMap.getProjection();
                Point coord= proj.toScreenLocation(latLng);
                //mostrar un avis con la informacion del Markers creado
                Toast.makeText(MainActivity.this,
                        "Click\n"+
                                "Lat: "+ latLng.latitude + "\n"+
                                "Lng: "+ latLng.longitude + "\n"+
                                "X: " + coord.x + "- Y: "+ coord.y,
                        Toast.LENGTH_SHORT).show();
            }
        });
        googleMap.setBuildingsEnabled(true);
        //inicializacion de controles y gestos
        UiSettings uiSettings = googleMap.getUiSettings();
        //habilitar control de zoom
        uiSettings.setZoomControlsEnabled(true);
        //habilitar inclinacion en el mapa
        uiSettings.setTiltGesturesEnabled(true);
    }

    //SPINNER para seleccionar el tipo de Mapa
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case 3:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //funcion para modifiicar lo colores del poligono
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.seek_red:
                red=progress;
                break;
            case R.id.seek_green:
                green=progress;
                break;
            case R.id.seek_blue:
                blue=progress;
                break;
        }
        if(polygon != null) {
            polygon.setStrokeColor(Color.rgb(red, green, blue));
            if (checkBox.isChecked())
                polygon.setFillColor(Color.rgb(red, green, blue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    //funcion para mostrar la LinearLayout de opciones del poligono
    public void mostrar(View button) {
        if (layoutAnimado.getVisibility() == View.GONE)
        {
            animar(true);
            layoutAnimado.setVisibility(View.VISIBLE);
        }
    }
    //funcion para ocultar la LinearLayout de opciones del poligono
    public void ocultar(View button) {
        if (layoutAnimado.getVisibility() == View.VISIBLE)
        {
            animar(false);
            layoutAnimado.setVisibility(View.GONE);
        }
    }
    //funcion para animar la aparicion del LinearLayout de opciones del poligono
    private void animar(boolean mostrar) {
        AnimationSet set = new AnimationSet(true);
        Animation animation = null;
        if (mostrar)
        {
            //desde la esquina inferior derecha a la superior izquierda
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        }
        else
        {    //desde la esquina superior izquierda a la esquina inferior derecha
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        }
        //duración en milisegundos
        animation.setDuration(500);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);
        layoutAnimado.setLayoutAnimation(controller);
        layoutAnimado.startAnimation(animation);
    }
}