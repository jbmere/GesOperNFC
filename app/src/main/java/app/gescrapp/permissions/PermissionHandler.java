package app.gescrapp.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Handles checks for permission
 * @author Daniel Clemente
 * @author Jorge García Paredes
 * */
public abstract class PermissionHandler {

    public static final int PERMISSIONS_LOCATION = 334;
    public static final int PERMISSIONS_CAMERA = 444;
    public static final int PERMISSIONS_EXT_STORAGE = 445;

    /**
     * Checks camera permissions and request them if necessary
     * @param context context of the activity that request the check
     * */
    public static void checkCameraPermission(final Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
/*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle("Permiso de lectura de identificador")
                        .setMessage("Esta aplicación necesita leer algunos datos de identificación del terminal para su correcto funcionamiento")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSIONS_CAMERA);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.

 */
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_CAMERA);
            //}
        }
    }
    /**
     * Checks external storage permissions and request them if necessary
     * @param context context of the activity that request the check
     * */
    public static void checkStoragePermission(final Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
/*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle("Permiso de lectura de identificador")
                        .setMessage("Esta aplicación necesita leer algunos datos de identificación del terminal para su correcto funcionamiento")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_EXT_STORAGE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.

 */
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_EXT_STORAGE);
            //}
        }
    }
    /**
     * Checks gps permissions and request them if necessary
     * @param context context of the activity that request the check
     * */
    public static void checkLocationPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
/*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle("Permiso de lectura de identificador")
                        .setMessage("Esta aplicación necesita leer algunos datos de identificación del terminal para su correcto funcionamiento")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSIONS_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.

 */
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_LOCATION);
            //}
        }
    }
    //Check other permissions
}
