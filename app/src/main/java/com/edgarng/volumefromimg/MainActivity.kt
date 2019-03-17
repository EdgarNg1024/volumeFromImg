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
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            procSrc2rectangle()
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

    fun procSrc2rectangle() {
        val srcImg = Mat()
        val srcBitmap = BitmapFactory.decodeResource(resources, R.mipmap.aa)
        Utils.bitmapToMat(srcBitmap, srcImg)//convert original bitmap to Mat, R G B.
        val dstImg = srcImg.clone()
        Imgproc.cvtColor(srcImg, srcImg, Imgproc.COLOR_RGB2GRAY)//rgbMat to gray grayMat
        Imgproc.threshold(srcImg, srcImg, 100.0, 255.0, Imgproc.THRESH_BINARY)

        var contours = mutableListOf<MatOfPoint>()
        var hierarcy = Mat()

        Imgproc.findContours(srcImg, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)
        var boundRect = mutableListOf<Rect>()
        var box = mutableListOf<RotatedRect>()
        var rect = arrayOf<Point>(Point(), Point(), Point(), Point())

        for (i in contours.indices) {
            box.add(Imgproc.minAreaRect(MatOfPoint2f(*contours[i].toArray())))
            boundRect.add(Imgproc.boundingRect(contours[i]))
            Imgproc.circle(dstImg, Point(box[i].center.x, box[i].center.y), 5, Scalar(0.0, 255.0, 0.0), -1, 8)
            box[i].points(rect)
            Imgproc.rectangle(
                dstImg, Point(boundRect[i].x.toDouble(), boundRect[i].y.toDouble()),
                Point(boundRect[i].x + boundRect[i].width.toDouble(), boundRect[i].y + boundRect[i].height.toDouble()),
                Scalar(0.0, 255.0, 0.0), 2, 8
            )
            for (j in 0..3) {
                Imgproc.line(dstImg, rect[j], rect[(j + 1) % 4], Scalar(0.0, 0.0, 255.0), 2, 8)
            }
        }
        Utils.matToBitmap(dstImg, srcBitmap) //convert mat to bitmap
        img.setImageBitmap(srcBitmap)

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
