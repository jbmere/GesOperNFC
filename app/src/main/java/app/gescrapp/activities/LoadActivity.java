package app.gescrapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import app.gescrapp.R;
import app.gescrapp.auxiliary.Truck;

import static app.gescrapp.auxiliary.HTTPClientFactory.getHttpsClient;

/**
 * Activity loaded when the app is open.
 * It displays a list of plates numbers downloaded from a database via HTTPS GET
 * @author Daniel Clemente
 * @author Jorge García Paredes
 * */
public class LoadActivity extends Activity {

    final String dataBaseDownloadURL = "https://apiict00.etsii.upm.es/matriculas.php";
    String stringDataDownloaded =  "";

    private TreeMap<String, Truck> mapOfTrucks = null;

    ListView lv_plate_numbers;
    ArrayAdapter<String> lvAdapter;
    EditText et_filterLV;

    List<String> plateNumbersArray;
    private int clickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Splash Screen http://web.archive.org/web/20180526144705/https://plus.google.com/+AndroidDevelopers/posts/Z1Wwainpjhd
        setTheme(R.style.AppTheme);
        //Descargar desde URL mientras está la Splash Screen, funciona bien si hay conexión a Internet
        new AsyncTaskDataDownload().execute(dataBaseDownloadURL);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        //SharedPreferences recovery
        Map<String, ?> prefsMap = prefs.getAll();
        if (prefsMap.containsKey(MainActivity.KEY_TRUCK_REG) && prefsMap.get(MainActivity.KEY_TRUCK_REG) != null) {     //If sharedprefs -> redirect to MainActivity
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra(MainActivity.KEY_REG, (String) prefsMap.get(MainActivity.KEY_TRUCK_REG));
            this.finish();
            this.overridePendingTransition(0, 0);
            this.startActivity(i);
            this.overridePendingTransition(0, 0);
            Log.d("LoadActSharedPrefs reg", "saved. REDIRECTION");
        }else {
            mapOfTrucks = new TreeMap<>();  //downdoaded collection
            plateNumbersArray = new ArrayList<String>();

            //ListView
            lvAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, plateNumbersArray);
            lv_plate_numbers = findViewById(R.id.lv_plate_numbers);
            lv_plate_numbers.setOnItemClickListener(listViewClickedHandler);
            lv_plate_numbers.setAdapter(lvAdapter);

            //Filter of the listView
            et_filterLV = findViewById(R.id.edit_text_search_filter);
            et_filterLV.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!plateNumbersArray.isEmpty()) {
                        (LoadActivity.this).lvAdapter.getFilter().filter(s);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    /**
     * Download and list truck registration
     * */
    private void listRegistration() {
        //Convertir texto JSON a un mapa -> mapOfTrucks
        //https://stackoverflow.com/questions/9968114/android-sending-https-get-request
        if(stringDataDownloaded!=null){
            try {
                parseJSONToTruckMap(new JSONObject(stringDataDownloaded));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Actualizar lista
            lvAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Converts an InputStream into a String
     * */
    //https://mkyong.com/java/how-to-convert-inputstream-to-string-in-java/
    static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8),1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * Parse function from JSON format from the URL, into a Trucks TreeMap
     * */
    private void parseJSONToTruckMap(JSONObject jsonObject){
        String s = "";
        Truck t = null;

        for (Iterator i = jsonObject.keys(); i.hasNext();) {        //Iterator through the keys in the JSONObject
            s = (String) i.next();
            try {
                t = new Truck(jsonObject.getJSONObject(s));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(!mapOfTrucks.containsKey(s)) {
                mapOfTrucks.put(s, t);
                //Añadir Matriculas a la listaView
                plateNumbersArray.add(t.getNumberPlate().toString());
            }
        }
        System.out.println(mapOfTrucks);
    }

    public void onClick(View view) throws IOException{
        int id = view.getId();
        switch (id) {
            case R.id.button_refresh:
                //Check URL to update data on the map
                new AsyncTaskDataDownload().execute(dataBaseDownloadURL);
                Toast.makeText(LoadActivity.this, getText(R.string.regs_retrieved), Toast.LENGTH_SHORT).show();
                listRegistration();
                break;

            case R.id.button_select_plate_number:
                if(clickedPosition>=0){
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra(MainActivity.KEY_REG, plateNumbersArray.get(clickedPosition));
                    this.finish();
                    //this.overridePendingTransition(0, 0);
                    this.startActivity(i);
                    //this.overridePendingTransition(0, 0);
                    //Log.d("LoadActivity", "Matricula (" + clickedPosition + "): " + plateNumbersArray.get(clickedPosition));
                }else{
                    Toast.makeText(LoadActivity.this, getText(R.string.select_button_reg), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Downloads list of trucks and their details from an URL.
     * The downloaded data is written in JSON
     */
    private class AsyncTaskDataDownload  extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... dataURL) {
            HttpResponse response = null;
            try {
                CloseableHttpClient client = (CloseableHttpClient) getHttpsClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(dataURL[0]));
                response = client.execute(request);
                stringDataDownloaded = convertStreamToString(response.getEntity().getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Create an action handling object as an anonymous class.
    private AdapterView.OnItemClickListener listViewClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // Do something in response to the click
            clickedPosition = position;
        }
    };
}


