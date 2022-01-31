package com.example.duos

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.example.duos.config.XAccessTokenInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApplicationClass : Application() {



    companion object{
        const val X_ACCESS_TOKEN: String = "X-ACCESS-TOKEN"         // JWT Token Key
        const val TAG: String = "TEMPLATE-APP"                      // Log, SharedPreference
        const val APP_DATABASE = "$TAG-DB"

        const val DEV_URL: String = "https://duos.co.kr/";       // 테스트 서버 주소
        const val PROD_URL: String = "https://api.template.com/"    // 실서버 주소
        const val BASE_URL: String = DEV_URL

        lateinit var mSharedPreferences: SharedPreferences
        lateinit var retrofit: Retrofit

        lateinit var baseApplication: ApplicationClass
        lateinit var progressDialog: AppCompatDialog

        fun getInstance(): ApplicationClass {
            return baseApplication
        }
    }

    override fun onCreate() {
        super.onCreate()

        baseApplication = this

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(XAccessTokenInterceptor()) // JWT 자동 헤더 전송
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mSharedPreferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }

    fun progressON(activity: Activity?, message: String?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            //progressSET(message)
        } else {
            progressDialog = AppCompatDialog(activity)
            progressDialog.setCancelable(false)
            progressDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog.setContentView(R.layout.progress_loading)
            progressDialog.show()
        }
        val img_loading_frame = progressDialog.findViewById<ImageView>(R.id.iv_frame_loading)
        val frameAnimation = img_loading_frame?.background as AnimationDrawable
        img_loading_frame.post { frameAnimation.start() }
//        val tv_progress_message = progressDialog.findViewById(R.id.tv_progress_message) as TextView
//        if (!TextUtils.isEmpty(message)) {
//            tv_progress_message.text = message
//        }
    }

//    fun progressSET(message: String?) {
//        if (progressDialog == null || !progressDialog.isShowing()) {
//            return
//        }
//        val tv_progress_message = progressDialog.findViewById(R.id.tv_progress_message) as TextView
//        if (!TextUtils.isEmpty(message)) {
//            tv_progress_message.text = message
//        }
//    }

    fun progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss()
        }
    }
}