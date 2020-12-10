package xyz.stuffium.metrics

import java.lang.reflect.Type

import com.google.gson.{JsonElement, JsonObject, JsonSerializationContext, JsonSerializer}

class RPReport(val modelA: String, val modelB: String, val results: List[Float]) {}

object RPReport extends JsonSerializer[RPReport] {

  override def serialize(src: RPReport, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val jo = new JsonObject

    jo.addProperty("modelA", src.modelA)
    jo.addProperty("modelB", src.modelB)
    jo.add("results", context.serialize(src.results.toArray))

    jo
  }

}