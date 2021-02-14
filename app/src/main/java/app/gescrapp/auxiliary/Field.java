package app.gescrapp.auxiliary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class Field {
    //int id = 0;
    String nfc = "";
    String field = "";
    String uds = "";

    public Field() {
    }

    /*public Field(int id, String nfc, String field, String uds) {
        this.id = id;
        this.nfc = nfc;
        this.field = field;
        this.uds = uds;
    }*/

    public Field(String nfc, String field, String uds) {
        this.nfc = nfc;
        this.field = field;
        this.uds = uds;
    }

    public Field(JSONObject jsonObject) throws JSONException {
        String s = "";

        for (Iterator i = jsonObject.keys(); i.hasNext();) {        //Iterator through the keys in the JSONObject ({"MATRICULA":"1122 GHL","TIPO":"Caja Abierta","KG Tara":"7500"})
            s = (String) i.next();
            switch (s){
                case "nfc":
                    this.nfc = jsonObject.getString(s);
                    break;
                case "field":
                    this.field = jsonObject.getString(s);
                    break;

                case "uds":
                    this.uds = jsonObject.getString(s);
                    break;
                default:
                    break;
            }   //End switch
        }   //End foreach
    }   //End constructor

    /*public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    */

    public String getNfc() {
        return nfc;
    }

    public void setNfc(String nfc) {
        this.nfc = nfc;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getUds() {
        return uds;
    }

    public void setUds(String uds) {
        this.uds = uds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return nfc.equals(field.nfc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nfc);
    }

    @Override
    public String toString() {
        return "Field{" +
                //"id=" + id + + ", "
                "nfc='" + nfc + '\'' +
                ", field='" + field + '\'' +
                ", uds='" + uds + '\'' +
                '}';
    }
}
