package etsiitdevs.caliope;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class AlumnoActivity extends AppCompatActivity {

    ProgressDialog dialog;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumno);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setTitle("");

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Caballar.ttf");
        ((TextView) findViewById(R.id.nombreApp)).setTypeface(type);


        rv = (RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);


        initializeData();
    }

    class Game {
        String nombre;
        int edad;
        String descripcion;
        String id;
        String desarrollador;
        int imagen;

        Game(String name, int age, int photoId) {
            nombre = name;
            edad = age;
            imagen = photoId;
        }
    }

    private List<Game> games;

    // This method creates an ArrayList that has three Person objects
    // Checkout the project associated with this tutorial on Github if
    // you want to use the same images.
    private void initializeData(){
        dialog = ProgressDialog.show(AlumnoActivity.this, "", "Cargando...", true);

        games = new ArrayList<Game>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = consultarJuegos();

                JsonElement rootElem = new JsonParser().parse(res);

                JsonObject json = rootElem.getAsJsonObject();

                JsonArray array = json.get("result").getAsJsonArray();

                for(int i=0; i<array.size(); i++)
                {
                    JsonObject game = array.get(i).getAsJsonObject();

                    Game g = new Game(game.get("nombre").getAsString(), game.get("edad").getAsInt(),
                            R.drawable.animales);

                    Log.d("debug", g.nombre);

                    games.add(g);
                }
                dialog.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RVAdapter adapter = new RVAdapter(games);
                        rv.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }


    private String consultarJuegos()
    {
        final String[] response = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        client.setTimeout(5000);
        RequestParams params = new RequestParams();
        client.post("http://caliope.hol.es/consultarJuegos.php", params,
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


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>
    {
        public class PersonViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView nombre;
            TextView edad;
            ImageView imagen;
            TextView jugar;

            PersonViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.cv);
                nombre = (TextView) itemView.findViewById(R.id.nombre);
                edad = (TextView) itemView.findViewById(R.id.edad);
                imagen = (ImageView) itemView.findViewById(R.id.imagen);
                jugar = (TextView) itemView.findViewById(R.id.jugar);

                jugar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(AlumnoActivity.this, JuegoAnimales.class);
                        startActivity(i);
                    }
                });
            }
        }
        List<Game> games;

        RVAdapter(List<Game> games) {
            this.games = games;
        }

        @Override
        public int getItemCount() {
            return games.size();
        }


        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            personViewHolder.nombre.setText(games.get(i).nombre);
            personViewHolder.edad.setText("Edad: " + games.get(i).edad);
            personViewHolder.imagen.setImageResource(games.get(i).imagen);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_settings:
                i = new Intent(AlumnoActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_editar:
                i = new Intent(AlumnoActivity.this, EditarAlumnoActivity.class);
                startActivity(i);
                return true;
            case R.id.action_salir:
                SessionManager sm = new SessionManager(AlumnoActivity.this);
                sm.logoutUser();
                i = new Intent(AlumnoActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
