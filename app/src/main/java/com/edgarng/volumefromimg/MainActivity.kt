package com.edgarng.volumefromimg

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            procSrc2Gray()
        }
    }

    val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> Log.i(TAG, "成功加载")
                else -> {
                    super.onManagerConnected(status)
                    Log.i(TAG, "加载失败")
                }
            }
        }
    }

    fun procSrc2Gray() {
        val rgbMat = Mat()
        val grayMat = Mat()
        var srcBitmap = BitmapFactory.decodeResource(resources, R.mipmap.aa)
        var grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565)
        Utils.bitmapToMat(srcBitmap, rgbMat)//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY)//rgbMat to gray grayMat
        Utils.matToBitmap(grayMat, grayBitmap) //convert mat to bitmap
        img.setImageBitmap(grayBitmap)
        Log.i(TAG, "procSrc2Gray sucess...")
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
