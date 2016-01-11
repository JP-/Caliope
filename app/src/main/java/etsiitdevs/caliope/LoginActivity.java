package etsiitdevs.caliope;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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

public class LoginActivity extends AppCompatActivity {

    private Button entrar;
    private TextView registrar;
    private EditText user;
    private EditText pass;
    private String username;
    private String password;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.getSupportActionBar().hide();


        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Caballar.ttf");
        ((TextView) findViewById(R.id.caliope)).setTypeface(type);

        entrar = ((Button) findViewById(R.id.entrar));
        registrar = ((TextView) findViewById(R.id.registrar));
        user = ((EditText) findViewById(R.id.enterusuario));
        pass = ((EditText) findViewById(R.id.enterpass));

        SessionManager sessionManager = new SessionManager(LoginActivity.this);
        if(sessionManager.isLoggedIn())
        {
            dialog = ProgressDialog.show(LoginActivity.this, "", "Cargando...", true);
            String tipo = sessionManager.getKeyTipo();
            if(tipo.equals("alumno")) {
                Intent i = new Intent(LoginActivity.this, AlumnoActivity.class);
                startActivity(i);
                finish();
            }
            else if(tipo.equals("tutor")) {
                Intent i = new Intent(LoginActivity.this, TutorActivity.class);
                startActivity(i);
                finish();
            }
        }


        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                username = user.getText().toString();
                password = pass.getText().toString();

                if (username.equals("") || password.equals(""))
                    Snackbar.make(v, "Rellena todos los campos",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else
                {
                    dialog = ProgressDialog.show(LoginActivity.this, "", "Cargando...", true);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String res = consultarUsuario(username, password);

                            JsonElement rootElem = new JsonParser().parse(res);

                            JsonObject json = rootElem.getAsJsonObject();

                            Log.d("debug", res);

                            if (json.get("result").getAsString().equals("1"))
                            {
                                SessionManager sm = new SessionManager(LoginActivity.this);
                                if(username.contains("@"))
                                {
                                    res = datosUsuario(username, "tutor");
                                    rootElem = new JsonParser().parse(res);
                                    json = rootElem.getAsJsonObject();

                                    String nombre = json.get("nombre").getAsString();
                                    String apellido = json.get("apellido").getAsString();


                                    sm.createLoginSession(nombre, apellido, username);

                                    dialog.cancel();
                                    Intent i = new Intent(LoginActivity.this, TutorActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                                else
                                {
                                    res = datosUsuario(username, "alumno");
                                    rootElem = new JsonParser().parse(res);
                                    json = rootElem.getAsJsonObject();

                                    String nombre = json.get("nombre").getAsString();
                                    String apellido = json.get("apellido").getAsString();
                                    String fecha = json.get("fechaNacimiento").getAsString();


                                    sm.createLoginSession(nombre, apellido, username, fecha);

                                    dialog.cancel();
                                    Intent i = new Intent(LoginActivity.this, AlumnoActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                            else if (json.get("result").getAsString().equals("0"))
                            {
                                dialog.cancel();
                                final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                                alert.setTitle("Error");
                                alert.setMessage("No se encuentra usuario con los datos proporcionados");
                                alert.setPositiveButton("OK", null);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alert.show();
                                    }
                                });
                            }
                            else
                            {
                                dialog.cancel();
                                final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                                alert.setTitle("Error");
                                alert.setMessage("vuelve a intentarlo");
                                alert.setPositiveButton("OK", null);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alert.show();
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });


        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(i);
                finish();
            }
        });

    }


    private String consultarUsuario(String username, String password)
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        client.post("http://caliope.hol.es/consultarUsuario.php", params,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
