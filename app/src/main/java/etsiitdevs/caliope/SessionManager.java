package etsiitdevs.caliope;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "wlm";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String STATUS = "status";
    private static final String STATUS_COLOR = "status_color";


    // User name (make variable public to access from outside)
    public static final String KEY_NOMBRE = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_APELLIDO = "apellido";


    public static final String KEY_CORREO = "correo";
    public static final String KEY_ID = "id";
    public static final String KEY_FECHA = "fecha";
    public static final String KEY_TIPO = "tipo";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String nombre, String apellido, String correo) {

        editor.putString(KEY_TIPO, "tutor");

        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NOMBRE, nombre);

        // Storing email in pref
        editor.putString(KEY_APELLIDO, apellido);

        editor.putString(KEY_CORREO, correo);

        // commit changes
        editor.commit();
    }

    public void createLoginSession(String nombre, String apellido, String id, String fecha) {

        editor.putString(KEY_TIPO, "alumno");

        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NOMBRE, nombre);

        // Storing email in pref
        editor.putString(KEY_APELLIDO, apellido);

        editor.putString(KEY_ID, id);

        editor.putString(KEY_FECHA, fecha);

        // commit changes
        editor.commit();
    }


    public String getKeyTipo()
    {
        return pref.getString(KEY_TIPO, "null");
    }

    public String getKeyNombre()
    {
        return pref.getString(KEY_NOMBRE, "null");
    }

    public String getKeyApellido()
    {
        return pref.getString(KEY_APELLIDO, "null");
    }

    public String getKeyId()
    {
        return pref.getString(KEY_ID, "null");
    }

    public String getKeyCorreo()
    {
        return pref.getString(KEY_CORREO, "null");
    }

    public String getKeyFecha()
    {
        return pref.getString(KEY_FECHA, "null");
    }


    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            //           Intent i = new Intent(_context, LoginScreen.class);
            //           // Closing all the Activities
            //           i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //
            //           // Add new Flag to start new Activity
            //           i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //
            //           // Staring Login Activity
            //           _context.startActivity(i);
        }

    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NOMBRE, pref.getString(KEY_NOMBRE, null));

        // user email id
        user.put(KEY_APELLIDO, pref.getString(KEY_APELLIDO, null));


        user.put(KEY_CORREO, pref.getString(KEY_CORREO, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        //       Intent i = new Intent(_context, LoginScreen.class);
        //       // Closing all the Activities
        //       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //
        //       // Add new Flag to start new Activity
        //       i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //
        //       // Staring Login Activity
        //       _context.startActivity(i);
    }

    /**
     * Quick check for login
     **/
// Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}

