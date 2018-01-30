import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.ReplaySubject
import java.lang.reflect.Type

class JsonReaderObservable<T>(elementType: Type, gson: Gson, private val reader: JsonReader) : Observable<T>() {

    private val subject = ReplaySubject.create<T>()

    init {
        reader.beginArray()
        while (reader.hasNext()) {
            subject.onNext(gson.fromJson(reader, elementType))
        }
        reader.endArray()
        subject.onComplete()
    }

    override fun subscribeActual(observer: Observer<in T>) {
        subject.subscribe(observer)
    }

}