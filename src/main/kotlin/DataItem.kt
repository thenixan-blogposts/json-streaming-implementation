import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.reactivex.Observable
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/*
{
    "about": "Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Etiam vel augue. Vestibulum rutrum rutrum neque. Aenean auctor gravida sem. Praesent id massa id nisl venenatis lacinia. Aenean sit amet justo. Morbi ut odio. Cras mi pede, malesuada in, imperdiet et, commodo vulputate, justo. In blandit ultrices enim.",
    "email": "mwilne0@tuttocitta.it",
    "first_name": "Joaquin",
    "gender": "Male",
    "hash": "77bf2c094b1204da4a399ce7d7be0eff383bc808c44de804541fe58277e8b97b",
    "id": 1,
    "ip_address": "67.92.203.230",
    "last_name": "Iannazzi",
    "name": {
        "first": "Michel",
        "second": "Wilne"
    },
    "tags": [
        "ac",
        "congue",
        "faucibus",
        "nibh",
        "faucibus"
    ]
}
 */

data class DataHolder(
        @SerializedName("data")
        val data: Array<DataItem>
)

data class NewDataHolder(
        @JsonAdapter(ObservableTypeAdapterFactory::class)
        @SerializedName("data")
        val data: Observable<DataItem>
)

data class DataItem(
        @SerializedName("about") val about: String,
        @SerializedName("email") val email: String,
        @SerializedName("first_name") val firstName: String,
        @SerializedName("gender") val gender: String,
        @SerializedName("hash") val hash: String,
        @SerializedName("id") val id: Long,
        @SerializedName("ip_address") val ipAddress: String,
        @SerializedName("last_name") val lastName: String,
        @SerializedName("name") val name: Name,
        @SerializedName("tags") val tags: Array<String>
)

data class Name(
        @SerializedName("first") val first: String,
        @SerializedName("second") val second: String
)


class ObservableTypeAdapterFactory : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        return if (Observable::class.java.isAssignableFrom(type.rawType)) {
            ObservableTypeAdapter<T>(type.type.getObservableParameterType(), gson) as TypeAdapter<T>
        } else null
    }

}

class ObservableTypeAdapter<T>(private val elementType: Type, private val gson: Gson) : TypeAdapter<Observable<T>>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, observable: Observable<T>) {
        out.beginArray()
        observable.blockingForEach { gson.toJson(it, elementType, out) }
        out.endArray()
    }

    override fun read(reader: JsonReader): Observable<T> {
        return JsonReaderObservable(elementType, gson, reader)
    }

}

private fun Type.getObservableParameterType(): Type = getTParameterType(Observable::class.java)

private fun Type.getTParameterType(expectedParameterizedType: Type): Type {
    if (expectedParameterizedType == this) {
        return expectedParameterizedType
    }
    if (this is ParameterizedType) {
        if (expectedParameterizedType == rawType) {
            val actualTypeArguments = actualTypeArguments
            if (actualTypeArguments.size == 1) {
                return actualTypeArguments[0]
            }
        }
    }
    throw IllegalArgumentException(toString())
}