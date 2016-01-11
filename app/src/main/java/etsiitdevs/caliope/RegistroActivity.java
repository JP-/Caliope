package etsiitdevs.caliope;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class RegistroActivity extends AppCompatActivity {

    private String tipo = null;
    private Button continuar;
    private RadioGroup radioButtonTipo;
    private String nombre;
    private String apellidos;
    private String pass;
    private String pass2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.getSupportActionBar().hide();

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Caballar.ttf");
        ((TextView) findViewById(R.id.caliope)).setTypeface(type);


        continuar = ((Button) findViewById(R.id.continuar));
        radioButtonTipo = ((RadioGroup) findViewById(R.id.entertipo));

        radioButtonTipo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.Alumno)
                    tipo = "Alumno";
                else if (checkedId == R.id.Tutor)
                    tipo = "Tutor";
            }
        });

        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nombre = ((TextView) findViewById(R.id.enternombre)).getText().toString();
                apellidos = ((TextView) findViewById(R.id.enterapellidos)).getText().toString();
                pass = ((TextView) findViewById(R.id.enterpass)).getText().toString();
                pass2 = ((TextView) findViewById(R.id.enterpass2)).getText().toString();
                //Log.d("App", pass + " " + pass2 + " " + nombre + " " + apellidos);

                if (tipo == null)
                    Snackbar.make(v, "Debes seleccionar un tipo",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else if (nombre.equals("") || apellidos.equals("") || pass.equals(""))
                    Snackbar.make(v, "Debes rellenar todos los campos",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else if (!pass.equals(pass2))
                    Snackbar.make(v, "Las contraseñas no coinciden",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else if (tipo == "Alumno")
                {
                    Intent i = new Intent(RegistroActivity.this, RegistroAlumnoActivity.class);
                    i.putExtra("nombre", nombre);
                    i.putExtra("apellidos", apellidos);
                    i.putExtra("pass", pass);
                    startActivity(i);
                    finish();
                }
                else if (tipo == "Tutor")
                {
                    Intent i = new Intent(RegistroActivity.this, RegistroTutorActivity.class);
                    i.putExtra("nombre", nombre);
                    i.putExtra("apellidos", apellidos);
                    i.putExtra("pass", pass);
                    startActivity(i);
                    finish();
                }

            }
        });
    }

}
