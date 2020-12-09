package xyz.stuffium.metrics

import java.lang.reflect.Type

import com.google.gson.{JsonElement, JsonObject, JsonSerializationContext, JsonSerializer}

class TableEntry(val recall: Float, val precision: Float, val interpolated: Boolean) {}

object TableEntry extends JsonSerializer[TableEntry] {

  override def serialize(src: TableEntry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val jo = new JsonObject

    jo.addProperty("recall", src.recall)
    jo.addProperty("precision", src.precision)
    jo.addProperty("interpolated", src.interpolated)

    jo
  }

}

