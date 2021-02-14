package app.gescrapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import app.gescrapp.R;
import app.gescrapp.auxiliary.Base64Handler;
import app.gescrapp.auxiliary.EntryDB;
import app.gescrapp.auxiliary.Field;
import app.gescrapp.auxiliary.InputDialog;
import app.gescrapp.permissions.PermissionHandler;

import static app.gescrapp.activities.LoadActivity.convertStreamToString;
import static app.gescrapp.auxiliary.HTTPClientFactory.getHttpsClient;

/**
 * Main Activity. Takes the selected plate number in Load Activity.
 * The user introduces a few variables more (type of action, NFC,
 * weight and duration of a certain event, optionally takes a picture) and send them via HTTPS POST into a Database.
 *
 * @author Daniel Clemente
 * @author Jorge García Paredes
 * */
public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String PREFS = "usrPrefs";
    public static final String KEY_TRUCK_REG = "tck_reg";
    public static final String KEY_TRUCK_TYPE = "tck_type";
    public static final String KEY_TRUCK_NFC_INFO = "tck_nfc";
    public static final String KEY_TRUCK_NFC_TS = "tck_ts";
    public static final String KEY_TRUCK_WEIGHT = "tck_weight";
    public static final String KEY_TRUCK_DURATION = "tck_dur";
    public static final String KEY_TRUCK_BASE64__PIC = "tck_pic";
    public static final String KEY_REG = "key_reg";
    public static final String KEY_WEIGHT = "key_weight";
    public static final String KEY_NFC = "key_nfc";
    public static final String KEY_TS = "key_ts";
    public static final String KEY_DURATION_MIN = "key_dur";

    private static final String DATA_BASE_UPLOAD_URL = "https://apiict00.etsii.upm.es/envio.php";
    private static final String DATA_BASE_DOWNLOAD_FIELDS_URL = "https://apiict00.etsii.upm.es/fields.php";

    Button regB, weightB, nfcB, picB, uplB, durationB;
    private Uri photoURI=null;

    EntryDB infoSent = new EntryDB();
    String folderpath = null;
    Lock lock= new ReentrantLock(true);

    //Controls any button is pressed
    boolean buttonPressed = false;
    String stringFieldsDownloaded = null;
    private TreeMap<String, Field> mapOfFields = null;
    int typeSelected = 0;
    boolean nfcValueB = false;

    //Control data added
    boolean bool_weight_added = false;
    boolean bool_duration_added = false;
    boolean bool_nfc_added = false;
    boolean bool_mandatory_ok = false;

    FusedLocationProviderClient fusedLocationClient;

    //TreeMap<Integer,Field> mapFields = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Download Fields from URL
        new AsyncTaskDataDownload().execute(DATA_BASE_DOWNLOAD_FIELDS_URL);

        String fieldValue = null;

        setContentView(R.layout.activity_main_constraint);
        regB = findViewById(R.id.button_reg);
        nfcB = findViewById(R.id.button_NFC);
        weightB = findViewById(R.id.button_weight);
        durationB = findViewById(R.id.button_time);
        uplB = findViewById(R.id.button_upload);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //Get file folder for the data update
        folderpath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator;

        //Recovers location on activity creation first time
        if (infoSent!=null || !infoSent.getLocation().equals("")) {
            Log.d("LOC", "ENTRA EN EL ONCREATE");
            getStringLocation();
        }else{
            Log.d("LOC", "NO ENTRA EN EL ONCREATE");
        }

        //Recovery and display of objects passed between intents
        try {
            infoSent.setRegistration(getIntent().getExtras().getString(MainActivity.KEY_REG));

            if (infoSent.getRegistration()==null) {
                //infoSent.setRegistration("9999NLL");
                //editor.putString(KEY_TRUCK_REG, infoSent.getRegistration());
                //editor.apply();
                Log.d("MainAct SharedPrefs", "REG was null!!!");
            }else{
                //Saves prefs
                editor.putString(KEY_TRUCK_REG, infoSent.getRegistration());
                editor.apply();
                //Log.d("MainAct SharedPrefs", " REG saved "+infoSent.getRegistration());
            }

        }catch (NullPointerException e){
            Log.d("MainAct", "NullPointerException KEY_REG");
        }

        try{
            infoSent.setNfcInfo(getIntent().getExtras().getString(MainActivity.KEY_NFC));
            if (infoSent.getNfcInfo()!= null && !infoSent.getNfcInfo().equals("")) {
                String nfcText = getText(R.string.edit_NFC) + infoSent.getNfcInfo();
                nfcB.setText(nfcText);
                nfcB.setBackground(ContextCompat.getDrawable(this, R.drawable.buttons_style_border_blue));
                nfcB.setTextColor(ContextCompat.getColor(this, R.color.colorAccentLight));
                //Save in SharedPrefs
                editor.putString(KEY_TRUCK_NFC_INFO, infoSent.getNfcInfo());
                editor.apply();
                //Log.d("MainAct nfcInfo:", infoSent.getNfcInfo());
            }
        }catch (NullPointerException e){
            Log.d("MainAct", "NullPointerException KEY_TS");
        }

        try{
            if(!getIntent().getExtras().getString(MainActivity.KEY_TS).equals("")&&getIntent().getExtras().getString(MainActivity.KEY_TS)!=null) {
                infoSent.setTimeStamp(getIntent().getExtras().getString(MainActivity.KEY_TS));
                editor.putString(KEY_TRUCK_NFC_TS, infoSent.getTimeStamp());
                editor.apply();
                //Log.d("MainAct TS:", infoSent.getTimeStamp());
            }
        }catch (NullPointerException e){
            Log.d("MainAct", "NullPointerException KEY_TS");
        }

        try{
            infoSent.setWeight(getIntent().getExtras().getString(MainActivity.KEY_WEIGHT));//Integer.getInteger(getIntent().getExtras().getString(MainActivity.KEY_WEIGHT));
            //Log.d("MainAct Weight:", infoSent.getWeight());
        }catch (NullPointerException e){
            infoSent.setWeight("0");
            Log.d("MainAct", "NullPointerException KEY_WEIGHT");
        }
        try{
            infoSent.setDuration(getIntent().getExtras().getString(MainActivity.KEY_DURATION_MIN));
            //Log.d("MainAct duration:", infoSent.getDuration());
        }catch (NullPointerException e){
            infoSent.setDuration("0");
            Log.d("MainAct", "NullPointerException KEY_DURATION_MIN");
        }
        if ( infoSent.getWeight()!=null && !infoSent.getWeight().equals("0")) {
            editor.putString(KEY_TRUCK_WEIGHT, infoSent.getWeight());
            editor.apply();
        }
        if (infoSent.getDuration() != null && !infoSent.getDuration().equals("0")) {
            editor.putString(KEY_TRUCK_DURATION, infoSent.getDuration());
            editor.apply();
        }

        //SharedPreferences recovery
        Map<String, ?> prefsMap = prefs.getAll();
        if (prefsMap != null) {
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_REG)) {
                infoSent.setRegistration((String) prefsMap.get(MainActivity.KEY_TRUCK_REG));
                String showReg = getString(R.string.registration) + infoSent.getRegistration();
                regB.setText(showReg);
                //Log.d("MainActSharedPrefs reg",infoSent.getRegistration());
            }
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_TYPE)) {
                infoSent.setType((String) prefsMap.get(MainActivity.KEY_TRUCK_TYPE));
                //Log.d("MainActSharedPrefs type",infoSent.getType());
            }
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_NFC_INFO)) {
                bool_nfc_added = true;
                infoSent.setNfcInfo((String) prefsMap.get(MainActivity.KEY_TRUCK_NFC_INFO));
                nfcB = findViewById(R.id.button_NFC);
                String nfcText = getText(R.string.edit_NFC) + infoSent.getNfcInfo();
                nfcB.setText(nfcText);
                nfcB.setBackground(ContextCompat.getDrawable(this, R.drawable.buttons_style_border_blue));
                nfcB.setTextColor(ContextCompat.getColor(this, R.color.colorAccentLight));
                //Log.d("MainActSharedPrefs nfc",infoSent.getNfcInfo());
            }
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_NFC_TS)) {
                infoSent.setTimeStamp((String) prefsMap.get(MainActivity.KEY_TRUCK_NFC_TS));
                //Log.d("MainActSharedPrefs ts",infoSent.getTimeStamp());
            }
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_WEIGHT)) {
                bool_weight_added = true;
                infoSent.setWeight((String) prefsMap.get(MainActivity.KEY_TRUCK_WEIGHT));
                String showWeight = getString(R.string.edit_weight) + infoSent.getWeight();
                weightB.setText(showWeight);
                weightB.setBackground(ContextCompat.getDrawable(this, R.drawable.buttons_style_border_blue));
                weightB.setTextColor(ContextCompat.getColor(this, R.color.colorAccentLight));
                //Log.d("MainActSharedPrefs weig",infoSent.getWeight());
            }
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_DURATION)) {
                bool_duration_added = true;
                infoSent.setDuration((String) prefsMap.get(MainActivity.KEY_TRUCK_DURATION));
                String showDur = getString(R.string.edit_time) + infoSent.getDuration();
                durationB.setText(showDur);
                durationB.setBackground(ContextCompat.getDrawable(this, R.drawable.buttons_style_border_blue));
                durationB.setTextColor(ContextCompat.getColor(this, R.color.colorAccentLight));
                //Log.d("MainActSharedPrefs time",infoSent.getDuration());
            }
            if (prefsMap.containsKey(MainActivity.KEY_TRUCK_BASE64__PIC)) {
                //infoSent.setPhoto(decodePicture((String) prefsMap.get(MainActivity.KEY_TRUCK_BASE64__PIC)));
                picB = findViewById(R.id.button_pic);
                infoSent.setPhotoBase64((String) prefsMap.get(MainActivity.KEY_TRUCK_BASE64__PIC));
                Bitmap photo = Base64Handler.decodeFromBase64(infoSent.getPhotoBase64());
                infoSent.setPhoto(photo);
                Bitmap thumbnail = Bitmap.createScaledBitmap(photo,200,200,false);
                Drawable buttonThumbnail = new BitmapDrawable(getResources(),thumbnail);
                picB.setBackground(buttonThumbnail);
                picB.setText(R.string.edit_picture);
                //Log.d("MainActSharedPrefs pic",infoSent.getPhoto().toString());
            }
        }

        //to avoid blocking main thread if no Internet connection
        if (internetIsConnected()) {
            boolean firstTime = true;
            do {
                if (firstTime) {
                    Log.d("MainAct", "Map downloading....");
                    firstTime = false;
                }
                //Loop to wait for the NFC data to be downloaded
            } while (stringFieldsDownloaded == null);
        }else {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.activate_internet_title))
                    .setMessage(getResources().getString(R.string.activate_internet_msg))
                    .setNegativeButton(getResources().getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent settingsIntent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.internet_activated), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();
        }
        //Parse the Read Fields into a TreeMap<String, Field>
        System.out.println("Fields after= "+ stringFieldsDownloaded);
        mapOfFields = new TreeMap<>();  //downdoaded collection
        if(stringFieldsDownloaded!=null){
            try {
                parseJSONToFieldMap(new JSONObject(stringFieldsDownloaded));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Compare read NFC string to downloaded map
        if (infoSent!=null && mapOfFields!=null) {
            Field f = new Field();
            Iterator<Field> it = mapOfFields.values().iterator();
            while(it.hasNext()){
                try {
                    f = (Field) it.next();
                    if (infoSent.getNfcInfo().equalsIgnoreCase(f.getNfc())) {
                        nfcValueB = true;
                        fieldValue = f.getField();
                    }
                }catch (NullPointerException e){
                    Log.d("MainAct exception","Don´t worry, this is usual the first time, but I had to write something");
                }
            }
        }

        //Choose option, if the value of the NFC Tag matched with a value that existed on the TreeMap. Saves the option as the EntryDB type
        if(nfcValueB){
            typeSelected = checkFieldValueAndSetOptionDisplayed(fieldValue);
            if (infoSent!=null) {
                infoSent.setType(Integer.toString(typeSelected));
                editor.putString(KEY_TRUCK_TYPE, infoSent.getType());
                editor.apply();
            }
        }

        //Change the layout according to the type
        displayOptions(typeSelected, weightB,(Button) findViewById(R.id.button_time), (Button) findViewById(R.id.button_pic), (Button) findViewById(R.id.button_upload));

        /*Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);*/
        //If all mandatory values are added, the upload button switches colour
        checkMandatoryValuesInOncreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!buttonPressed) {
            deleteSharedPrefs();
            Log.d("MainAct onDestroy", "SharedPrefs deleted");
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    /**
     * Buttons control
     * @param view activity view
     * */
    public void onClick(View view) {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        buttonPressed = true;
        int id = view.getId();
        switch (id) {
            case (R.id.button_reg):{
                //return to previous activity, no extras. ¡¡NO deleteSharedPrefs!! (saves registration)
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();                 //Quita todos los datos de guardados
                editor.apply();
                Intent i = new Intent(this, LoadActivity.class);
                this.finish();
                this.startActivity(i);
                break;
            }
            case (R.id.button_NFC):{
                //Redirect to NFCActivity to handle NFC read and timestamp
                deleteSharedPrefs(); //To forget if something saved
                Intent i = new Intent(this, NFCActivity.class);
                this.finish();
                this.startActivity(i);
                break;
            }
            case (R.id.button_weight):{
                //Open dialog with input
                InputDialog enterWeightDialog = new InputDialog(MainActivity.this,true,false);
                enterWeightDialog.show();
                break;
            }
            case (R.id.button_time):{
                //Open dialog with input
                InputDialog enterTimeDialog = new InputDialog(MainActivity.this,false,true);
                enterTimeDialog.show();
                break;
            }
            case (R.id.button_pic):{
                //call external camera and get miniature
                dispatchTakePictureIntent();
                break;
            }
            case (R.id.button_upload):{
                checkMandatoryValuesWhenUploadIsPressed(); //In order to show in red the mandatory values that were not yet introducet
                if (bool_mandatory_ok) {
                    buttonPressed = false;      //In order to delete SharedPrefs
                    if (internetIsConnected()) {
                        //Checks the GPS is active, shows a confirmation dialog and sends if user confirms
                        checkGpsAndSend();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(getResources().getString(R.string.activate_internet_title))
                                .setMessage(getResources().getString(R.string.activate_internet_msg))
                                .setNegativeButton(getResources().getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent settingsIntent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                                        startActivity(settingsIntent);
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.internet_activated), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                })
                                .create()
                                .show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.send_button_toast_msg), Toast.LENGTH_LONG).show();
                }
                break;
            }
            default:{
                //Log.d("MainActivity", getString(R.string.error_main_buttons));
                break;
            }
        }
    }

    /**
     * Checks if all mandatory values are set and highlight the ones that are not
     * */
    private void checkMandatoryValuesWhenUploadIsPressed() {
        switch (typeSelected) {
            case 1:     //No mandatory fields, only NFC
            case 2:{
                if (bool_nfc_added) {
                    bool_mandatory_ok = true;
                } else {
                    bool_mandatory_ok = false;
                    nfcB.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.buttons_style_border_red));
                }
                break;
            }
            case 3:{
                if (bool_weight_added && bool_nfc_added) {
                    bool_mandatory_ok = true;
                } else {
                    bool_mandatory_ok = false;
                    if (!bool_weight_added) {
                        weightB.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.buttons_style_border_red));
                    }
                    if (!bool_nfc_added) {
                        nfcB.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.buttons_style_border_red));
                    }
                }
                break;
            }
            case 4: {
                if (bool_duration_added && bool_nfc_added) {
                    bool_mandatory_ok = true;
                } else {
                    bool_mandatory_ok = false;
                    if (!bool_duration_added) {
                        durationB.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.buttons_style_border_red));
                    }
                    if (!bool_nfc_added) {
                        nfcB.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.buttons_style_border_red));
                    }
                }
                break;
            }
            default: {
                bool_mandatory_ok = false;
                break;
            }
        }
    }

    /**
     * Checks if all mandatory values are set and highlight the send button if they are
     * */
    private void checkMandatoryValuesInOncreate() {
        switch (typeSelected) {
            case 1:
            case 2: {
                if (bool_nfc_added) {
                    uplB.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                }
                break;
            }
            case 3: {
                if (bool_weight_added && bool_nfc_added) {
                    uplB.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                }
                break;
            }
            case 4: {
                if (bool_duration_added && bool_nfc_added) {
                    uplB.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                }
                break;
            }
        }
    }

    /**
     * Checks if the device has an active Internet connection
     * */
    private boolean internetIsConnected(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //we are connected to a network
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        return  connected;
    }

    /**
     * Checks if the device has an active GPS and calls send function
     * */
    public void checkGpsAndSend(){
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        boolean gpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsActive) {
            //Se informa al usuario de que debe activarlo, se bloquea la actividad hasta que lo active
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.activate_gps_title))
                    .setMessage(getResources().getString(R.string.activate_gps_msg))
                    .setNegativeButton(getResources().getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.gps_activated), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();
        }
        else{
            getStringLocation();
            //Log.d("MainAct nfc", "Location:" + infoSent.getLocation());
            createConfirmDialogAndSend();
        }
    }

    /**
     * Sends data after user confirmation
     * */
    private void createConfirmDialogAndSend (){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        String sent = infoSent.toString();
                        //Subir a la URL
                        //String fileName = makeFileToUpload(sent);
                        //if (fileName != null) {
                            new AsyncTaskDataUpload().execute(sent);
                        //}
                        deleteSharedPrefs();
                        if (fusedLocationClient != null) {
                            fusedLocationClient.removeLocationUpdates(mLocationCallback);
                        }
                        //resetButtons();
                        Intent i = new Intent(getIntent());
                        //Button Reset, you have to send a value, otherwise it will send previous values
                        i.putExtra(KEY_WEIGHT, "0");
                        i.putExtra(KEY_NFC, "");
                        i.putExtra(KEY_TS, "");
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        if (fusedLocationClient != null) {
                            fusedLocationClient.removeLocationUpdates(mLocationCallback);
                        }
                        onBackPressed();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_confirm_msg).setPositiveButton(R.string.ok, dialogClickListener)
                .setNegativeButton(R.string.dismiss, dialogClickListener).show();
    }

    /**
     * Gets user location and stores it in an EntryDB
     * */
    private void getStringLocation(){
        if ((ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        infoSent.setLocation(location.getLatitude() + "/" + location.getLongitude());
                        //Log.d("MainAct Location ", location.getLatitude() + "/" + location.getLongitude());
                    } else {
                        getRealTimeLocation(fusedLocationClient);
                        Log.d("MainAct Location Error", "Location not found!");
                    }
                }

            });
        }else{
            PermissionHandler.checkLocationPermission(this);
        }
    }

    /**
     * Gets device real time location
     * */
    private void getRealTimeLocation(FusedLocationProviderClient fusedLocationClient) {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Se comprueba que el GPS del terminal está activado
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                LocationRequest mLocationRequest = new LocationRequest();

                mLocationRequest.setInterval(10000); //intervalo de refresco
                mLocationRequest.setFastestInterval(10000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } else {
                PermissionHandler.checkLocationPermission(this);
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("TAG", "Location updated");
                infoSent.setLocation(location.getLatitude() + "/" + location.getLongitude());
            }
        }
    };

    /**
     * Calls external camera activity
     * */
    private void dispatchTakePictureIntent() {
        if ((ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)){     //Permission granted
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.d("MainActivity", "IOException taking photo");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    try {
                        photoURI = FileProvider.getUriForFile(this,
                                "app.gescrapp.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        //takePictureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 3000);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        Log.d("MainActivity", "Camera accessed.");
                    } catch (IllegalArgumentException e) {
                        Log.d("MainActivity", "IllegalArgument:");
                    }
                }
            }
        }else{
            PermissionHandler.checkCameraPermission(MainActivity.this);
            PermissionHandler.checkStoragePermission(MainActivity.this);
        }
    }
    /**
     * Gets result from external camera activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        picB = findViewById(R.id.button_pic);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                Bitmap photoSend = Bitmap.createScaledBitmap(photo,720,1080,true);
                infoSent.setPhotoBase64(Base64Handler.encodeToBase64(photoSend));
                SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_TRUCK_BASE64__PIC, infoSent.getPhotoBase64());
                editor.apply();
                //Create thumbnail for button
                //Bitmap thumbnail = Bitmap.createScaledBitmap(Base64Handler.decodeFromBase64(infoSent.getPhotoBase64()),200,200,false);  //TEST DECODE
                Bitmap thumbnail = Bitmap.createScaledBitmap(photo,200,200,true);
                Drawable buttonThumbnail = new BitmapDrawable(getResources(),thumbnail);
                picB.setBackground(buttonThumbnail);
                picB.setText(R.string.edit_picture);
                infoSent.setPhoto(photoSend);
                //Encode photo and store it in infoSent
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates temporal image file, this is destroyed when app is closed
     * */
    private File createImageFile() throws IOException {
        String currentPhotoPath;
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Displays the right buttons according given an option
     * */
    private void displayOptions(int optionDisplayed, final Button weightB, final Button timeB, final Button picB, final Button uploadB){
        TextView tvInitialMsg = findViewById(R.id.tvInitialMsg);
        switch (optionDisplayed) {
            case 1:
            case 2: {
                //Display only photo button
                tvInitialMsg.setVisibility(View.GONE);
                weightB.setVisibility(View.GONE);
                timeB.setVisibility(View.GONE);
                picB.setVisibility(View.VISIBLE);
                picB.getLayoutParams().width = (ConstraintLayout.LayoutParams.MATCH_PARENT);
                uploadB.setVisibility(View.VISIBLE);
                break;
            }
            case 3: {
                //Display weight and photo
                tvInitialMsg.setVisibility(View.GONE);
                weightB.setVisibility(View.VISIBLE);
                timeB.setVisibility(View.GONE);
                picB.setVisibility(View.VISIBLE);
                uploadB.setVisibility(View.VISIBLE);
                break;
            }
            case 4: {
                //Display time and photo
                tvInitialMsg.setVisibility(View.GONE);
                weightB.setVisibility(View.GONE);
                timeB.setVisibility(View.VISIBLE);
                picB.setVisibility(View.VISIBLE);
                uploadB.setVisibility(View.VISIBLE);
                break;
            }
            default: {
                tvInitialMsg.setVisibility(View.VISIBLE);
                weightB.setVisibility(View.GONE);
                timeB.setVisibility(View.GONE);
                picB.setVisibility(View.GONE);
                uploadB.setVisibility(View.GONE);
                break;
            }
        }
    }

    /**
     * Asks for user permission
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults != null) {
            switch (requestCode) {
                case PermissionHandler.PERMISSIONS_LOCATION:{
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getStringLocation();
                    } else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                case PermissionHandler.PERMISSIONS_CAMERA: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("permission", "granted");
                        dispatchTakePictureIntent();
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission
                        Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case PermissionHandler.PERMISSIONS_EXT_STORAGE: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("permission STORAGE", "granted");
                        if (buttonPressed){     //Means we are trying to access camera
                            dispatchTakePictureIntent();
                        }else {                 //Means we are trying to send data
                            getStringLocation();
                            //Log.d("MainAct nfc", "Location:" + infoSent.getLocation());
                            createConfirmDialogAndSend();
                        }
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission
                        Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

    /**Deletes all SharedPreferences data except for the registration*/
    private void deleteSharedPrefs (){
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();                 //Quita todos los datos de guardados
        editor.apply();
        //Se conserva la matrícula
        editor.putString(KEY_TRUCK_REG, infoSent.getRegistration());
        editor.apply();
    }

    private String makeFileToUpload(String stringToSend){
        //***Create text file and write data into it***
        System.out.println("writting to text file");
        lock.lock(); //Lock begin
        String time_now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        FileWriter fw = null;
        String txtname = folderpath + "truck_" + time_now;
        File txtfile = new File(txtname + ".txt");
        if (txtfile.exists()) {                 //check if txt file already existed, then adds number at the end of the its name
            File ftxt = new File(folderpath);    //if exist count the number of files with this name
            int ntxt = 0;
            for (File file : ftxt.listFiles()) {
                if (file.isFile() && (file.getName().startsWith("truck_" + time_now)) && (file.getName().endsWith(".txt"))) {
                    ntxt++;
                }
            }
            txtname = txtname + Integer.toString(ntxt);
        }
        try {
            fw = new FileWriter(txtname + ".txt", true);

            BufferedWriter bufferWriter = new BufferedWriter(fw, stringToSend.length());
            //bufferWriter.write(sb.toString());
            bufferWriter.write(stringToSend);
            bufferWriter.close();
            Log.d("MainAct DataToFile", txtname + " OK");

        } catch (IOException e) {
            Log.d("MainAct DataToFile", "Error writing to and closing file:" + e.getMessage());
            lock.unlock(); //Release lock
            return null;
        }
        lock.unlock();  //Release lock
        return txtname + ".txt";
    }

    private class AsyncTaskDataUpload extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... sendData) {

            String entryDBJSONString = sendData[0];
            if (entryDBJSONString != null) {
                try {
                    CloseableHttpClient httpsclient = (CloseableHttpClient) getHttpsClient();
                    HttpPost httppost = new HttpPost(DATA_BASE_UPLOAD_URL);

                    // Request parameters and other properties.
                    //Sending JSON format of the data
                    List<NameValuePair> dataToSend = infoSent.entryDBValuesList();
                    //This would send NameValuePair of the data
                    //List<NameValuePair> dataToSend = new ArrayList<NameValuePair>(1);
                    //dataToSend.add(new BasicNameValuePair("entryToDBPost", infoSent.toString()));
                    httppost.setEntity(new UrlEncodedFormEntity(dataToSend, "UTF-8"));

                    //Execute and get the response.
                    HttpResponse response = httpsclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    if (entity != null) {
                        try (InputStream instream = entity.getContent()) {
                            // do something useful
                            Log.d("MainAct DataUploaded", convertStreamToString(instream));
                            //Toast.makeText(MainActivity.this, "POST response" + xxx , Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("MainAct DataUpload", String.valueOf(e));
                }
            }
            Log.d("MainAct send", entryDBJSONString);
            return null;
        }
    }

    /**
     * Given a field from the NFC ("", "peso" or "duracion")
     * Returns a value to display the weight or duration or just picture.
     * */
    private int checkFieldValueAndSetOptionDisplayed(String fieldValue) {
        int option = 0;

        if(fieldValue!=null){
            switch (fieldValue) {
                case "peso":
                case "Peso": {
                    option = 3;
                    break;
                }
                case "duracion":
                case "Duracion": {
                    option = 4;
                    break;
                }
                default:
                    option = 1;
                    break;
            }
        }else{
            option = 1;
        }

        return option;
    }

    /**
     * Parse function from JSON format from the URL, into a Fields TreeMap
     * */
    private void parseJSONToFieldMap(JSONObject jsonObject){
        String s = "";
        Field f = null;

        for (Iterator i = jsonObject.keys(); i.hasNext();) {        //Iterator through the keys in the JSONObject
            s = (String) i.next();
            try {
                f = new Field(jsonObject.getJSONObject(s));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(!mapOfFields.containsKey(s)) {
                mapOfFields.put(s, f);
            }
        }
        System.out.println(mapOfFields);
    }

    /**
     * Downloads list of NFC Tag fields and their details from an URL.
     * The downloaded data is written in JSON
     *
     * @return*/
    public class AsyncTaskDataDownload  extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... dataURL) {
            HttpResponse response = null;
            try {
                CloseableHttpClient client = (CloseableHttpClient) getHttpsClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(dataURL[0]));
                response = client.execute(request);
                stringFieldsDownloaded = convertStreamToString(response.getEntity().getContent());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
