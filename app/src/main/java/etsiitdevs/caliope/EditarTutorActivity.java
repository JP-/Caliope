package etsiitdevs.caliope;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class EditarTutorActivity extends AppCompatActivity {

    private String nombre;
    private String apellidos;
    private String pass;
    private String pass2;
    private String correo;

    private Button confirmar;
    private Button baja;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tutor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        setTitle("Editar datos de usuario");


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SessionManager sessionManager = new SessionManager(EditarTutorActivity.this);

        ((TextView) findViewById(R.id.enternombre)).setText(sessionManager.getKeyNombre());
        ((TextView) findViewById(R.id.enterapellidos)).setText(sessionManager.getKeyApellido());
        ((TextView) findViewById(R.id.entercorreo)).setText(sessionManager.getKeyCorreo());


        confirmar = ((Button) findViewById(R.id.confirmar));
        baja = ((Button) findViewById(R.id.darbaja));


        baja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(EditarTutorActivity.this, "", "Cargando...", true);

                correo = ((EditText) findViewById(R.id.entercorreo)).getText().toString();

                String res = darBaja();

                JsonElement rootElem = new JsonParser().parse(res);

                JsonObject json = rootElem.getAsJsonObject();

                if (json.get("result").getAsString().equals("1"))
                {
                    dialog.cancel();
                    new SessionManager(EditarTutorActivity.this).logoutUser();
                    Intent i = new Intent(EditarTutorActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                else
                {
                    viewAlert("Vuelve a intentarlo");
                }
            }
        });


        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = ProgressDialog.show(EditarTutorActivity.this, "", "Cargando...", true);

                nombre = ((TextView) findViewById(R.id.enternombre)).getText().toString();
                apellidos = ((TextView) findViewById(R.id.enterapellidos)).getText().toString();
                pass = ((TextView) findViewById(R.id.enterpass)).getText().toString();
                pass2 = ((TextView) findViewById(R.id.enterpass2)).getText().toString();
                correo = ((TextView) findViewById(R.id.entercorreo)).getText().toString();


                if (nombre.equals("") || apellidos.equals("") || pass.equals(""))
                    Snackbar.make(v, "Debes rellenar todos los campos",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else if (!pass.equals(pass2))
                    Snackbar.make(v, "Las contraseñas no coinciden",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else {
                    String res = editarTutor();
                    JsonElement rootElem = new JsonParser().parse(res);

                    JsonObject json = rootElem.getAsJsonObject();

                    if (json.get("result").getAsString().equals("1")) {
                        SessionManager sm = new SessionManager(EditarTutorActivity.this);

                        res = datosUsuario(correo, "tutor");

                        Log.d("debug", res);

                        rootElem = new JsonParser().parse(res);
                        json = rootElem.getAsJsonObject();

                        String nombre = json.get("nombre").getAsString();
                        String apellido = json.get("apellido").getAsString();
                        String correo = json.get("email").getAsString();

                        sm.logoutUser();

                        sm.createLoginSession(nombre, apellido, correo);

                        dialog.cancel();
                        final AlertDialog.Builder alert = new AlertDialog.Builder(EditarTutorActivity.this);
                        alert.setTitle("Correcto");
                        alert.setMessage("Los datos se han modificado correctamente");
                        alert.setPositiveButton("OK", null);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alert.show();
                            }
                        });
                    } else {
                        viewAlert("Vuelve a intentarlo");
                    }
                }
            }
        });
    }

    private String editarTutor()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("nombre", nombre);
        params.put("apellidos", apellidos);
        params.put("pass", pass);
        params.put("correo", correo);
        params.put("tipo", "tutor");

        Log.d("debug", params.toString());

        client.post("http://caliope.hol.es/editarUsuario.php", params,
                new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        response[0] = res;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        response[0] = res;
                    }
                }
        );

        return response[0];
    }

    public void viewAlert(String msg)
    {
        dialog.cancel();
        final AlertDialog.Builder alert = new AlertDialog.Builder(EditarTutorActivity.this);
        alert.setTitle("Error");
        alert.setMessage(msg);
        alert.setPositiveButton("OK", null);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alert.show();
            }
        });
    }

    private String darBaja()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("id", correo);
        params.put("tipo", "alumno");
        client.post("http://caliope.hol.es/darBaja.php", params,
                new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        response[0] = res;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        response[0] = res;
                    }
                }
        );

        return response[0];
    }


    private String datosUsuario(String id, String tipo)
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("tipo", tipo);
        Log.d("debug", params.toString());
        client.post("http://caliope.hol.es/datosUsuario.php", params,
                new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        response[0] = res;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        response[0] = res;
                    }
                }
        );

        return response[0];
    }

    @Override
    public void onDestroy()
    {
        if(dialog != null)
            dialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        if(dialog != null)
            dialog.dismiss();
        super.onPause();
    }

}
