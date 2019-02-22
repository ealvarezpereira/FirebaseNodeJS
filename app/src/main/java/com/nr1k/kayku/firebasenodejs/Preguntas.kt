package com.nr1k.kayku.firebasenodejs

/**
 * Para guardar los valores que quiero introducir/actualizar en la base de datos
 * Contiene un HashMap con los datos, ya que las funciones que utilizaré necesitan como parámetro
 * un HashMap
 */
data class Preguntas(var id: String = "", var respuesta: String = "")