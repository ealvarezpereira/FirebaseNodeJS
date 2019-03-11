package com.nr1k.kayku.firebasenodejs

import android.annotation.SuppressLint
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val hashRespuesta = HashMap<String, Any>()
    val hashScore = HashMap<String, Any>()
    var score = HashMap<String,Int>()
    lateinit var objPreguntas: Preguntas
    lateinit var objScore: Score
    var n: Int = 0
    var arrayPreguntas: ArrayList<Preguntas> = ArrayList()
    lateinit var token: String

    @SuppressLint("NewApi")
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
        tiempo.max = 5
        tiempo.progress = 5

        // boton de la plantilla
        fab.setOnClickListener { view ->
            if (n < arrayPreguntas.size){

                    hashRespuesta.put(Build.MODEL+"_"+Build.DEVICE,Respuestas(FCMToken!!,"","false"))
                    databaseJugadores!!.updateChildren(hashRespuesta)
                    txtPregunta.text = arrayPreguntas[n].id
                    boSi.isEnabled = true
                    boNo.isEnabled = true
                    fab.isEnabled = false

                    GlobalScope.launch {

                        tiempo.progress = 5
                        while (tiempo.progress != 0){
                            delay(1000)
                            tiempo.progress = tiempo.progress-1

                        }
                        if(tiempo.progress == 0 && boSi.isEnabled){
                            runOnUiThread {
                                run {
                                    ponerScore("comes caca")
                                    boSi.isEnabled = false
                                    boNo.isEnabled = false
                                }
                            }
                        }
                    }

                }else {

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
                FCMToken = FirebaseInstanceId.getInstance().getToken()
                Log.d("tokennn","token: "+FCMToken.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG,"Error escribiendo datos ${e}")
            }
        }
        // inicializo el listener para los eventos de la basededatos
        initPreguntas()
        initJugadores()
        initScore()
    }


    fun ponerScore(resp:String){
        fab.isEnabled = true
        tiempo.progress = 0
        if (resp.equals(arrayPreguntas[n].respuesta)){
            txtPregunta.text ="Correcto!"
            hashRespuesta.put(Build.MODEL+"_"+Build.DEVICE,Respuestas(FCMToken!!,"true","false"))
            databaseJugadores!!.updateChildren(hashRespuesta)
        }else{
            txtPregunta.text = "Comes caca"
            hashRespuesta.put(Build.MODEL+"_"+Build.DEVICE, Respuestas(FCMToken!!,"false","false"))
            databaseJugadores!!.updateChildren(hashRespuesta)
        }
        n++
    }

    override fun onDestroy() {
        super.onDestroy()
        hashRespuesta.put(Build.MODEL+"_"+Build.DEVICE, Respuestas(FCMToken!!,"","true"))
        databaseJugadores!!.updateChildren(hashRespuesta)
    }

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
    private fun initScore() {
        val childEventListener = object : ChildEventListener {
            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d(TAG, "Datos borrados: " + p0.key)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("xddd",p0.getValue().toString())
                    if(p0.key==Build.MODEL+"_"+Build.DEVICE)
                        txtScore.text="Score: "+p0.getValue().toString()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos movidos")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                // onChildAdded() capturamos la key
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error cancelacion")
            }
        }
        // attach el evenListener a la basededatos
        databaseScore!!.addChildEventListener(childEventListener)
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