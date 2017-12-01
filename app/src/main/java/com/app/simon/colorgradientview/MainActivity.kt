package com.app.simon.colorgradientview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk15.listeners.onClick

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start.onClick {
            val num = et_num.text.toString()
            sweep_view.setProgress(num.toInt())
        }
    }
}
