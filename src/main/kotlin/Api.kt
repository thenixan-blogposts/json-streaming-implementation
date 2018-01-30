import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming

interface Api {

    @GET("thenixan-blogposts/json-streaming-data/master/one-large-file.json")
    fun loadDataInUsualWay(): Single<DataHolder>

    @Streaming
    @GET("thenixan-blogposts/json-streaming-data/master/one-large-file.json")
    fun loadDataWithNewTypeConverter(): Single<NewDataHolder>

    @Streaming
    @GET("thenixan-blogposts/json-streaming-data/master/one-large-file.json")
    fun loadDataWithoutAnyParsingAtAll(): Single<ResponseBody>

}

val service = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Api::class.java)