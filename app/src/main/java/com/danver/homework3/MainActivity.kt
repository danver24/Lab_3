package com.danver.homework3

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var newsAdapter: NewsAdapter

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newsAdapter = NewsAdapter(emptyList())
        findViewById<RecyclerView>(R.id.recyclerView).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.recyclerView).adapter = newsAdapter

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            val keyword = findViewById<EditText>(R.id.keywordEditText).text.toString().trim()
            if (keyword.isNotEmpty()) {
                makeRequest(keyword)
            } else {
                Toast.makeText(this, "Введите ключевое слово", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun makeRequest(keyword: String) {
        val url = "https://newsdata.io/api/1/news?apikey=pub_34656676d6455bf8c7d76f087344573451a8a&language=ru&q=${keyword}"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body()?.string()
                    if (!jsonResponse.isNullOrEmpty()) {
                        try {
                            val jsonObject = JsonParser().parse(jsonResponse).asJsonObject
                            Log.d("TAAG", jsonObject.toString())
                            val news: MutableList<News> = mutableListOf()
                            jsonObject["results"].asJsonArray.forEach {
                                val news_item: News = News(
                                    it.asJsonObject["title"].toString(),
                                    it.asJsonObject["content"].toString(),
                                    it.asJsonObject["link"].toString()
                                )
                                news.add(news_item)
                            }
                            runOnUiThread(object : Runnable{
                                @SuppressLint("NotifyDataSetChanged")
                                override fun run() {
                                    newsAdapter = NewsAdapter(news)
                                    findViewById<RecyclerView>(R.id.recyclerView).adapter = newsAdapter
                                    newsAdapter.notifyDataSetChanged()
                                }

                            })

                        } catch (e: JsonSyntaxException) {
                            Toast.makeText(baseContext, "Error parsing JSON", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "Empty response body", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("ERROR", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("ERROR", "Request failed: ${e.message}")
            }
        })
    }
}
