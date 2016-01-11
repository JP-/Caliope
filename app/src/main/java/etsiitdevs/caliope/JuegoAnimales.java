package etsiitdevs.caliope;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import android.os.Handler;

public class JuegoAnimales extends AppCompatActivity {

    public class Imagen
    {
        ImageView img;
        String nombre;
        int estado;
        int id;
        int idsec;

        Imagen(ImageView i, String n, int id, int ids)
        {
            img = i;
            nombre = n;
            estado = 0;
            this.id = id;
            idsec = ids;
            img.setImageResource(id);
        }
    }

    ArrayList<Imagen> imagenes;

    HashMap<String, String> datos;

    Imagen seleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_animales);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("");

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Caballar.ttf");
        ((TextView) findViewById(R.id.titulo)).setTypeface(type);


        ((TextView) findViewById(R.id.salir)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        datos = new HashMap<>();

        imagenes = new ArrayList<>();
        seleccionada = null;

        Imagen img = new Imagen((ImageView) findViewById(R.id.img1), "gallina", R.drawable.gallina2,
                R.drawable.gallina3);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img2), "pato", R.drawable.pato2,
                R.drawable.pato3);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img3), "perro", R.drawable.perro2,
                R.drawable.perro3);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img4), "burro", R.drawable.burro2,
                R.drawable.burro3);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img5), "pez", R.drawable.pez2,
                R.drawable.pez3);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img6), "gato", R.drawable.gato2,
                R.drawable.gato3);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img7), "gato", R.drawable.gato,
                R.drawable.gato4);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img8), "pez", R.drawable.pez,
                R.drawable.pez4);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img9), "pato", R.drawable.pato,
                R.drawable.pato4);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img10), "perro", R.drawable.perro,
                R.drawable.perro4);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img11), "burro", R.drawable.burro,
                R.drawable.burro4);
        imagenes.add(img);
        img = new Imagen((ImageView) findViewById(R.id.img12), "gallina", R.drawable.gallina,
                R.drawable.gallina4);
        imagenes.add(img);


        for(int i=0; i<imagenes.size(); i++)
        {
            final Imagen imagen = imagenes.get(i);
            final int fi = i;
            imagen.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (imagen.estado == 0)
                    {
                        if(seleccionada != null)
                        {
                            imagen.img.animate().rotationYBy(180);
                            if(imagen.nombre.equals(seleccionada.nombre))
                            {
                                imagen.img.setImageResource(R.drawable.dorso2);

                                seleccionada.img.animate().rotationYBy(180);
                                seleccionada.img.setImageResource(R.drawable.dorso2);

                                imagen.estado = seleccionada.estado = 2;

                                seleccionada = null;


                                if(comprobarJuego())
                                {
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(JuegoAnimales.this);
                                    alert.setTitle("¡Enhorabuena!");
                                    alert.setMessage("Has resuelto correctamente el juego.");
                                    alert.setPositiveButton("OK", null);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                finish();
                                            }
                                        });
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            alert.show();
                                        }
                                    });
                                }
                            }
                            else
                            {
                                imagen.img.setImageResource(R.drawable.dorso3);

                                seleccionada.img.animate().rotationYBy(180);
                                seleccionada.img.setImageResource(R.drawable.dorso3);

                                imagen.estado = seleccionada.estado = 2;

                                final Imagen des = seleccionada;

                                seleccionada = null;

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        imagen.img.animate().rotationYBy(180);
                                        imagen.img.setImageResource(imagen.id);

                                        des.img.animate().rotationYBy(180);
                                        des.img.setImageResource(des.id);


                                        imagen.estado = des.estado = 0;
                                    }
                                }, 500);
                            }
                        }
                        else
                        {
                            imagen.img.setImageResource(imagen.idsec);
                            imagen.estado = 1;
                            seleccionada = imagen;
                        }
                    }
                    else if (imagen.estado == 1)
                    {
                        imagen.img.setImageResource(imagen.id);
                        imagen.estado = 0;
                        seleccionada = null;
                    }
                }
            });
        }
    }


    public boolean comprobarJuego()
    {
        boolean terminado = true;
        for(Imagen img : imagenes)
            if (img.estado != 2)
                terminado = false;

        return terminado;
    }

}
