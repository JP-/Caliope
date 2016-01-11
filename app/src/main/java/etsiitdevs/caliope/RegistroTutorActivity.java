package etsiitdevs.caliope;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
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

public class RegistroTutorActivity extends AppCompatActivity {

    private String nombre;
    private String apellidos;
    private String pass;
    private String correo;

    private EditText entercorreo;

    private Button confirmar;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_tutor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.getSupportActionBar().hide();

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Caballar.ttf");
        ((TextView) findViewById(R.id.caliope)).setTypeface(type);


        Intent i = getIntent();
        nombre = i.getStringExtra("nombre");
        apellidos = i.getStringExtra("apellidos");
        pass = i.getStringExtra("pass");

        entercorreo = ((EditText)findViewById(R.id.entercorreo));
        confirmar = ((Button) findViewById(R.id.confirmar));


        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correo = entercorreo.getText().toString();

                if (correo.equals(""))
                    Snackbar.make(v, "Debes rellenar todos los campos",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else {

                    consultarTutor();

                    dialog = ProgressDialog.show(RegistroTutorActivity.this, "",
                            "Cargando...", true);
                }
            }
        });
    }

    public  void  consultarTutor()
    {

        // consulta los datos del alumno y lo registra si es posible

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String[] response = new String[1];
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(5000);
                RequestParams params = new RequestParams();
                params.put("tutor", correo);
                client.post("http://caliope.hol.es/consultarTutor.php", params,
                        new TextHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String res) {
                                response[0] = res;

                                Log.d("debug", "consulta de alumno con respuesta: " + response[0]);

                                JsonElement rootElem = new JsonParser().parse(response[0]);

                                JsonObject json = rootElem.getAsJsonObject();

                                // si no existe un tutor con ese correo
                                if (json.get("result").getAsString().equals("0"))
                                {
                                    response[0] = registrarTutor();
                                    rootElem = new JsonParser().parse(response[0]);

                                    json = rootElem.getAsJsonObject();

                                    Log.d("debug", "registro de alumno con respuesta: " + response[0]);

                                    if (json.get("result").getAsString().equals("1")) {
                                        dialog.cancel();


                                        new SessionManager(RegistroTutorActivity.this).
                                                createLoginSession(nombre, apellidos, correo);


                                        Intent i = new Intent(RegistroTutorActivity.this, TutorActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        viewAlert("Vuelve a intentarlo");
                                    }
                                } else if (json.get("result").getAsString().equals("1")) {
                                    viewAlert("Nombre de usuario no disponible.");
                                } else {
                                    viewAlert("Vuelve a intentarlo");
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                viewAlert("Vuelve a intentarlo");
                            }
                        }
                );
            }
        }).start();

    }

    public  String  registrarTutor()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("nombre", nombre);
        params.put("apellidos", apellidos);
        params.put("pass", pass);
        params.put("tipo", "tutor");
        params.put("correo", correo);

        client.post("http://caliope.hol.es/registrarUsuario.php", params,
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
        final AlertDialog.Builder alert = new AlertDialog.Builder(RegistroTutorActivity.this);
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
