package app.gescrapp.auxiliary;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

/**
 * Custom class to classify the JSON information downloaded from the Database (through its URL address)
 *  @author Daniel Clemente
 *  @author Jorge García Paredes
 * */
public class Truck {
    private String numberPlate = null;
    private String towType = null;
    private float tareWeight = 0;  //Kilogrames with int máx = 2147483647 = 2147tons

    public Truck(String numberPlate, String towType, float tareWeight){
        this.numberPlate = numberPlate;
        this.towType = towType;
        this.tareWeight = tareWeight;
    }

    public Truck(JSONObject jsonObject) throws JSONException {
        String s = "";

        for (Iterator i = jsonObject.keys(); i.hasNext();) {        //Iterator through the keys in the JSONObject ({"MATRICULA":"1122 GHL","TIPO":"Caja Abierta","KG Tara":"7500"})
            s = (String) i.next();
            switch (s){
                case "MATRICULA":
                    this.numberPlate = jsonObject.getString(s);
                    break;
                case "TIPO":
                    this.towType = jsonObject.getString(s);
                    break;

                case "KG Tara":
                    this.tareWeight = Integer.parseInt(jsonObject.getString(s));
                    break;
                default:
                    break;
            }   //End switch
        }   //End foreach
    }   //End constructor

    //Equals and HashCode methods include only the numberPlate String
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Truck truck = (Truck) o;
        return numberPlate.equals(truck.numberPlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberPlate);
    }

    //Getters
    public String getNumberPlate() {
        return numberPlate;
    }
    public String getTowType() {
        return towType;
    }
    public float getTareWeight() {
        return tareWeight;
    }

    @NonNull
    public String toString() {
        return "Truck{" +
                "numberPlate='" + numberPlate + '\'' +
                ", towType='" + towType + '\'' +
                ", tareWeight=" + tareWeight +
                '}';
    }
}

