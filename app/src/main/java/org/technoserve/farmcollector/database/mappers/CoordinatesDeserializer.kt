package org.technoserve.farmcollector.database.mappers

//import com.google.gson.JsonDeserializationContext
//import com.google.gson.JsonDeserializer
//import com.google.gson.JsonElement
//import java.lang.reflect.Type
//
//
//class CoordinatesDeserializer : JsonDeserializer<List<Pair<Double?, Double?>>> {
//    override fun deserialize(
//        json: JsonElement,
//        typeOfT: Type,
//        context: JsonDeserializationContext
//    ): List<Pair<Double?, Double?>> {
//        val result = mutableListOf<Pair<Double?, Double?>>()
//        val jsonArray = json.asJsonArray
//
//        for (element in jsonArray) {
//            val coord = element.asJsonArray
//            result.add(Pair(
//                coord[0]?.asDouble?: 0.0,
//                coord[1]?.asDouble?: 0.0
//            ))
//        }
//        return result
//    }
//}
//
//// Extension function to safely parse JSON elements
//private fun JsonElement?.asDoubleOrNull(): Double? {
//    return if (this != null && !this.isJsonNull) this.asDouble else null
//}
