package xyz.stuffium.metrics

import java.lang.reflect.Type

import com.google.gson.{JsonElement, JsonObject, JsonSerializationContext, JsonSerializer}

class ModelReport(val model: String, val results: List[QueryResult], val p5: Float, val p10: Float, val mrr: Float) {}

object ModelReport extends JsonSerializer[ModelReport] {
  override def serialize(src: ModelReport, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val jo = new JsonObject

    jo.addProperty("model", src.model)
    jo.addProperty("precisionAt5", src.p5)
    jo.addProperty("precisionAt10", src.p10)
    jo.addProperty("mrr", src.mrr)
    jo.add("queries", context.serialize(src.results.toArray))

    jo
  }
}
