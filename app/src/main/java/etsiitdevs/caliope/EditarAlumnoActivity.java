package etsiitdevs.caliope;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class EditarAlumnoActivity extends AppCompatActivity {


    private static Button buttonfecha;
    private static int year;
    private static int month;
    private static int day;

    private String nombre;
    private String apellidos;
    private String pass;
    private String pass2;
    private String tutor;
    private String id;
    private String correo;
    private static String fecha;

    private EditText entercorreo;
    private RadioGroup enterTutor;
    private Button confirmar;
    private Button baja;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_alumno);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Editar datos de usuario");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SessionManager sessionManager = new SessionManager(EditarAlumnoActivity.this);

        ((TextView) findViewById(R.id.enternombre)).setText(sessionManager.getKeyNombre());
        ((TextView) findViewById(R.id.enterapellidos)).setText(sessionManager.getKeyApellido());
        ((TextView) findViewById(R.id.enterid)).setText(sessionManager.getKeyId());


        confirmar = ((Button) findViewById(R.id.confirmar));
        baja = ((Button) findViewById(R.id.darbaja));
        entercorreo = ((EditText)findViewById(R.id.entercorreo));
        enterTutor = ((RadioGroup)findViewById(R.id.entertipo));

        enterTutor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.si) {
                    tutor = "si";
                    entercorreo.setEnabled(true);
                } else if (checkedId == R.id.no) {
                    tutor = "no";
                    entercorreo.setText("");
                    entercorreo.setEnabled(false);
                }
            }
        });


        buttonfecha = ((Button)findViewById(R.id.enterfecha));

        String f = sessionManager.getKeyFecha();
        String[] f2 = f.split("-");
        year = Integer.valueOf(f2[0]);
        month = Integer.valueOf(f2[1]);
        day = Integer.valueOf(f2[2]);
        buttonfecha.setText(String.valueOf(day) + "/" + String.valueOf(month) + "/" + String.valueOf(year));
        buttonfecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });


        baja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(EditarAlumnoActivity.this, "", "Cargando...", true);

                id = ((EditText) findViewById(R.id.enterid)).getText().toString();

                String res = darBaja();

                JsonElement rootElem = new JsonParser().parse(res);

                JsonObject json = rootElem.getAsJsonObject();

                if (json.get("result").getAsString().equals("1"))
                {
                    dialog.cancel();
                    new SessionManager(EditarAlumnoActivity.this).logoutUser();
                    Intent i = new Intent(EditarAlumnoActivity.this, LoginActivity.class);
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

                nombre = ((TextView) findViewById(R.id.enternombre)).getText().toString();
                apellidos = ((TextView) findViewById(R.id.enterapellidos)).getText().toString();
                pass = ((TextView) findViewById(R.id.enterpass)).getText().toString();
                pass2 = ((TextView) findViewById(R.id.enterpass2)).getText().toString();
                id = ((TextView) findViewById(R.id.enterid)).getText().toString();
                if(tutor == "si")
                    correo = entercorreo.getText().toString();

                if (nombre.equals("") || apellidos.equals("") || pass.equals("") || id.equals(""))
                    Snackbar.make(v, "Debes rellenar todos los campos",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else if (!pass.equals(pass2))
                    Snackbar.make(v, "Las contraseñas no coinciden",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else
                {
                    dialog = ProgressDialog.show(EditarAlumnoActivity.this, "", "Cargando...", true);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            // si se le ha asignado un tutor
                            if (tutor == "si") {
                                Log.d("debug", "si tiene tutor");
                                String res = consultarTutor();

                                JsonElement rootElem = new JsonParser().parse(res);

                                JsonObject json = rootElem.getAsJsonObject();

                                // si no existe un tutor con ese correo
                                if (json.get("result").getAsString().equals("0"))
                                {
                                    viewAlert("No existe ningún tutor con email: " + correo);
                                }
                                else if (json.get("result").getAsString().equals("-1"))
                                {
                                    viewAlert("Vuelve a intentarlo");
                                }
                                // si hay un tutor con ese correo
                                else if (json.get("result").getAsString().equals("1"))
                                {
                                    // se registra al alumno
                                    res = editarAlumno();
                                    rootElem = new JsonParser().parse(res);

                                    json = rootElem.getAsJsonObject();

                                    if (json.get("result").getAsString().equals("1")) {
                                        SessionManager sm = new SessionManager(EditarAlumnoActivity.this);
                                        res = datosUsuario(id, "alumno");
                                        rootElem = new JsonParser().parse(res);
                                        json = rootElem.getAsJsonObject();

                                        String nombre = json.get("nombre").getAsString();
                                        String apellido = json.get("apellido").getAsString();
                                        String fecha = json.get("fechaNacimiento").getAsString();

                                        sm.logoutUser();

                                        sm.createLoginSession(nombre, apellido, id, fecha);

                                        dialog.cancel();
                                        final AlertDialog.Builder alert = new AlertDialog.Builder(EditarAlumnoActivity.this);
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
                            // si no tiene tutor
                            else {
                                // se registra al alumno
                                String res = editarAlumno();

                                Log.d("debug", res);
                                JsonElement rootElem = new JsonParser().parse(res);

                                JsonObject json = rootElem.getAsJsonObject();

                                if (json.get("result").getAsString().equals("1")) {
                                    SessionManager sm = new SessionManager(EditarAlumnoActivity.this);
                                    res = datosUsuario(id, "alumno");
                                    rootElem = new JsonParser().parse(res);
                                    json = rootElem.getAsJsonObject();

                                    String nombre = json.get("nombre").getAsString();
                                    String apellido = json.get("apellido").getAsString();
                                    String fecha = json.get("fechaNacimiento").getAsString();

                                    sm.logoutUser();

                                    sm.createLoginSession(nombre, apellido, id, fecha);

                                    dialog.cancel();
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(EditarAlumnoActivity.this);
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
                    }).start();
                }
            }
        });
    }


    public void viewAlert(String msg)
    {
        dialog.cancel();
        final AlertDialog.Builder alert = new AlertDialog.Builder(EditarAlumnoActivity.this);
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


    public String consultarTutor()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("tutor", tutor);
        client.post("http://caliope.hol.es/consultarTutor.php", params,
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


    private String darBaja()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("id", id);
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


    private String editarAlumno()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("nombre", nombre);
        params.put("apellidos", apellidos);
        params.put("pass", pass);
        params.put("tipo", "alumno");
        params.put("fecha", String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(day));
        params.put("id", id);
        if(tutor == "si")
        {
            params.put("tutor", correo);
        }
        else
        {
            params.put("tutor", "null");
        }

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


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            year = y;
            month = m;
            day = d;
            buttonfecha.setText(String.valueOf(day) + "/" + String.valueOf(month+1) + "/" + String.valueOf(year));
            fecha = String.valueOf(day) + "/" + String.valueOf(month+1) + "/" + String.valueOf(year);
        }
    }


    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "Fecha de nacimiento");
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
