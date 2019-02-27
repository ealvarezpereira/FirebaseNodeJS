package com.nr1k.kayku.firebasenodejs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.*
import com.google.firebase.database.ChildEventListener
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.content_main.*
import android.os.Build

class MainActivity : AppCompatActivity() {
    // para filtrar los logs
    val TAG = "Servicio"

    // referencia de la base de datos
    private var databasePreguntas: DatabaseReference? = null
    private var databaseJugadores: DatabaseReference? = null
    private var databaseScore: DatabaseReference? = null
    // Token del dispositivo
    private var FCMToken: String? = null
    // key unica creada automaticamente al añadir un child
    lateinit var key: String
    // para actualizar los datos necesito un hash map
    val hasRespuesta = HashMap<String, Any>()
    val hashScore = HashMap<String, Any>()
    lateinit var objPreguntas: Preguntas
    var n: Int = 0
    var arrayPreguntas: ArrayList<Preguntas> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        boSi.isEnabled = false
        boNo.isEnabled = false
        // referencia a la base de datos del proyecto en firebase
        databasePreguntas = FirebaseDatabase.getInstance().getReference("/juego")
        databaseJugadores = FirebaseDatabase.getInstance().getReference("/jugadores")
        databaseScore = FirebaseDatabase.getInstance().getReference("/puntuacion")

        hashScore.put(Build.MODEL+"_"+Build.DEVICE,0)
        databaseScore!!.updateChildren(hashScore)


        // boton de la plantilla
        fab.setOnClickListener { view ->
            if (n < arrayPreguntas.size){

                    hasRespuesta.put(Build.MODEL+"_"+Build.DEVICE,Respuestas(""))
                    databaseJugadores!!.updateChildren(hasRespuesta)
                    txtPregunta.text = arrayPreguntas[n].id
                    boSi.isEnabled = true
                    boNo.isEnabled = true
                }else{
                    boSi.isEnabled = false
                    boNo.isEnabled = false
            }
        }

        boSi.setOnClickListener{view ->
            ponerScore("verdadero")
            boSi.isEnabled = false
            boNo.isEnabled = false
        }

        boNo.setOnClickListener{view ->
            ponerScore("falso")
            boSi.isEnabled = false
            boNo.isEnabled = false
        }

        // solo lo llamo cuando arranco la app
        // evito que cuando se pasa por el onCreate vuelva a ejecutarse
        if (savedInstanceState == null) {
            try {
                // Obtengo el token del dispositivo.
                FCMToken = FirebaseInstanceId.getInstance().token

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG,"Error escribiendo datos ${e}")
            }
        }
        // inicializo el listener para los eventos de la basededatos
        initPreguntas()
        initJugadores()
    }


    fun ponerScore(resp:String){
        if (resp.equals(arrayPreguntas[n].respuesta)){
            txtPregunta.text ="Correcto!"
            hasRespuesta.put(Build.MODEL+"_"+Build.DEVICE,Respuestas("true"))
            databaseJugadores!!.updateChildren(hasRespuesta)
        }else{
            txtPregunta.text = "Comes caca"
            hasRespuesta.put(Build.MODEL+"_"+Build.DEVICE, Respuestas("false"))
            databaseJugadores!!.updateChildren(hasRespuesta)
        }
        n++
    }


    /**
     * Listener para los distintos eventos de la base de datos
     */
    private fun initJugadores() {
        val childEventListener = object : ChildEventListener {
            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d(TAG, "Datos borrados: " + p0.key)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos cambiados: " + (p0.getValue() as HashMap<*,*>).toString())
                Log.d("prueba",p0.child("score").getValue().toString())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos movidos")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error cancelacion")
            }
        }
        // attach el evenListener a la basededatos
        databaseJugadores!!.addChildEventListener(childEventListener)
    }

    private fun initPreguntas() {
        val childEventListener = object : ChildEventListener {
            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d(TAG, "Datos borrados: " + p0.key)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos cambiados: " + (p0.getValue() as HashMap<*,*>).toString())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos movidos")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                // onChildAdded() capturamos la key
                objPreguntas = p0.getValue(Preguntas::class.java)!!
                arrayPreguntas.add(objPreguntas)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error cancelacion")
            }
        }
        // attach el evenListener a la basededatos
        databasePreguntas!!.addChildEventListener(childEventListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}