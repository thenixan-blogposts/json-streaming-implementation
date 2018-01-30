import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import io.reactivex.Observable

fun main(args: Array<String>) {

    println("Common usage of retrofit")
    benchmark {
        service.loadDataInUsualWay()
                .subscribe({ result ->
                    println("Size is: ${result.data.size}")
                }, { error ->
                    error.printStackTrace()
                })
    }



    println("Retrofit with custom TypeConverter")
    benchmark {
        service.loadDataWithNewTypeConverter()
                .flatMapObservable {
                    it.data
                }
                .count()
                .subscribe({ result ->
                    println("Size is: $result")
                }, { error -> error.printStackTrace() })
    }



    println("Retrofit streaming")
    benchmark {
        service.loadDataWithoutAnyParsingAtAll()
                .flatMapObservable { responseBody ->
                    Observable.create<DataItem> { emitter ->
                        JsonReader(responseBody.charStream())
                                .use { reader ->
                                    while (reader.hasNext()) {
                                        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                            reader.beginObject()
                                            if (reader.nextName() == "data") {
                                                reader.beginArray()
                                                while (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
                                                    emitter.onNext(Gson().fromJson<DataItem>(reader, DataItem::class.java))
                                                }
                                            }
                                        }
                                    }
                                    emitter.onComplete()
                                }
                    }
                }
                .count()
                .subscribe({ result ->
                    println("Size is: $result")
                }, { error -> error.printStackTrace() })
    }


}

private fun benchmark(f: () -> Unit) {
    System.gc()
    val startTime = System.currentTimeMillis()
    val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    f.invoke()

    System.gc()
    val finishTime = System.currentTimeMillis()
    val finishMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    val elapsedMemory = finishMemory - startMemory
    val elapsedTime = finishTime - startTime

    println("Elapsed time: $elapsedTime")
    println("Used memory: $elapsedMemory")
}


