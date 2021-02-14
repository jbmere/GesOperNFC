package app.gescrapp.auxiliary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
/**
 * Encodes and decodes images in Bitmap format to string in Base64 encoding
 * @author Daniel Clemente
 * @author Jorge García Paredes
 * */
public abstract class Base64Handler {
    /**@param image  image to encode*/
    public static String encodeToBase64(Bitmap image){
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }
    /**@param base64Image string with a Base64 encoded image
     * @return Bitmap with the decoded image*/
    public static Bitmap decodeFromBase64(String base64Image){
        byte[] decodedBytes = Base64.decode(base64Image, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}