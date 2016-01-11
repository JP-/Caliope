package etsiitdevs.caliope;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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

public class RegistroAlumnoActivity extends AppCompatActivity {

    private static Button buttonfecha;
    private static int year;
    private static int month;
    private static int day;

    private String nombre;
    private String apellidos;
    private String pass;
    private static String fecha;
    private String id;
    private String tutor;
    private String correo;

    private EditText enterid;
    private EditText entercorreo;
    private RadioGroup enterTutor;
    private Button confirmar;

    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_alumno);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.getSupportActionBar().hide();

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Caballar.ttf");
        ((TextView) findViewById(R.id.caliope)).setTypeface(type);

        Intent i = getIntent();
        nombre = i.getStringExtra("nombre");
        apellidos = i.getStringExtra("apellidos");
        pass = i.getStringExtra("pass");

        enterid = ((EditText)findViewById(R.id.enterid));
        entercorreo = ((EditText)findViewById(R.id.entercorreo));
        enterTutor = ((RadioGroup)findViewById(R.id.entertipo));
        confirmar = ((Button) findViewById(R.id.confirmar));

        enterTutor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.si) {
                    tutor = "si";
                    entercorreo.setEnabled(true);
                }
                else if (checkedId == R.id.no) {
                    tutor = "no";
                    entercorreo.setText("");
                    entercorreo.setEnabled(false);
                }
            }
        });

        buttonfecha = ((Button)findViewById(R.id.enterfecha));
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        buttonfecha.setText(String.valueOf(day) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));
        buttonfecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });


        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tutor == "si")
                    correo = entercorreo.getText().toString();

                id = enterid.getText().toString();

                if (id.equals(""))
                    Snackbar.make(v, "Debes rellenar todos los campos",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else {

                    consultarAlumno();

                    dialog = ProgressDialog.show(RegistroAlumnoActivity.this, "",
                            "Cargando...", true);
                }
            }
        });


    }

    public  void  consultarAlumno()
    {

        // consulta los datos del alumno y lo registra si es posible

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String[] response = new String[1];
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(5000);
                RequestParams params = new RequestParams();
                params.put("alumno", id);
                client.post("http://caliope.hol.es/consultarAlumno.php", params,
                        new TextHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String res) {
                                response[0] = res;

                                Log.d("debug", "consulta de alumno con respuesta: " + response[0]);

                                JsonElement rootElem = new JsonParser().parse(response[0]);

                                JsonObject json = rootElem.getAsJsonObject();

                                // si no existe un alumno con ese id
                                if (json.get("result").getAsString().equals("0"))
                                {
                                    // si se le ha asignado un tutor
                                    if (tutor == "si") {
                                        Log.d("debug", "si tiene tutor");
                                        response[0] = consultarTutor();

                                        rootElem = new JsonParser().parse(response[0]);

                                        json = rootElem.getAsJsonObject();

                                        Log.d("debug", "consulta de tutor con respuesta: " + response[0]);

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
                                            response[0] = registrarAlumno();
                                            rootElem = new JsonParser().parse(response[0]);

                                            json = rootElem.getAsJsonObject();

                                            Log.d("debug", "registro de alumno con respuesta: " + response[0]);

                                            if (json.get("result").getAsString().equals("1")) {

                                                new SessionManager(RegistroAlumnoActivity.this).
                                                        createLoginSession(nombre, apellidos, id, fecha);

                                                dialog.cancel();
                                                Intent i = new Intent(RegistroAlumnoActivity.this, AlumnoActivity.class);
                                                startActivity(i);
                                                finish();
                                            } else {
                                                viewAlert("Vuelve a intentarlo");
                                            }
                                        }
                                    }
                                    // si no tiene tutor
                                    else {
                                        Log.d("debug", "no tiene tutor");

                                        // se registra al alumno
                                        response[0] = registrarAlumno();
                                        rootElem = new JsonParser().parse(response[0]);

                                        json = rootElem.getAsJsonObject();

                                        Log.d("debug", "registro de alumno con respuesta: " + response[0]);

                                        if (json.get("result").getAsString().equals("1")) {
                                            dialog.cancel();
                                            Intent i = new Intent(RegistroAlumnoActivity.this, AlumnoActivity.class);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            viewAlert("Vuelve a intentarlo");
                                        }
                                    }
                                // si ya existe un alumno con ese id
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


    public void viewAlert(String msg)
    {
        dialog.cancel();
        final AlertDialog.Builder alert = new AlertDialog.Builder(RegistroAlumnoActivity.this);
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


    public  String  registrarAlumno()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        params.put("nombre", nombre);
        params.put("apellidos", apellidos);
        params.put("pass", pass);
        params.put("tipo", "alumno");
        params.put("id", id);
        if (tutor == "si")
            params.put("tutor", correo);
        else
            params.put("tutor", "null");

        params.put("fecha", String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(day));

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


    public String consultarTutor()
    {
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
