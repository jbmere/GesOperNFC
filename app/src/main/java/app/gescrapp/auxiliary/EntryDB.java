package app.gescrapp.auxiliary;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  A class with all the inserted information of the truck
 *  @author Daniel Clemente
 *  @author Jorge García Paredes
 * */
public class EntryDB {
    String registration;
    String type;
    String weight;
    String nfcInfo;
    String timeStamp;
    String location;
    String photoBase64;
    Bitmap photo;
    String duration;

    public EntryDB() {
        this.registration = "";
        this.type = "";
        this.weight = "0";
        this.nfcInfo = "";
        this.timeStamp = "";
        this.location = "";
        this.photoBase64 = "";
        this.duration = "";
        this.photo = null;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {this.weight = weight; }

    public String getNfcInfo() {
        return nfcInfo;
    }

    public void setNfcInfo(String nfcInfo) {
        this.nfcInfo = nfcInfo;
    }

    public String getTimeStamp() { return timeStamp; }

    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getPhotoBase64() { return photoBase64; }

    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getDuration() { return duration; }

    public void setDuration(String duration) { this.duration = duration; }

    @NonNull
    @Override
    public String toString() {  //DB JSON format
        return "{'time':'" + this.timeStamp +
                "','plateNumber':'" + registration +
                "','type':'" + type +
                "','nfcInfo':'" + nfcInfo +
                "','location':'" + location +
                "','weight':'" + weight +
                "','duration':'" + duration +
                "','photo':'" + photoBase64 + "'}";
    }

    public List<NameValuePair> entryDBValuesList(){
        List<NameValuePair> dataToSend = new ArrayList<NameValuePair>(7);

        dataToSend.add(new BasicNameValuePair("time", timeStamp));
        dataToSend.add(new BasicNameValuePair("plateNumber", registration));
        dataToSend.add(new BasicNameValuePair("type", type));
        dataToSend.add(new BasicNameValuePair("nfcInfo", nfcInfo));
        dataToSend.add(new BasicNameValuePair("location", location));
        dataToSend.add(new BasicNameValuePair("weight", weight));
        dataToSend.add(new BasicNameValuePair("duration", duration));
        dataToSend.add(new BasicNameValuePair("photo", photoBase64));

        return dataToSend;
    }
}
