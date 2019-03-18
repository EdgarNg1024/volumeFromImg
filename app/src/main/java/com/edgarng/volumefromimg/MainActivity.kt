package com.edgarng.volumefromimg

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
    var showTxt = ""
    val imgPath = R.mipmap.aa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        img.setImageResource(imgPath)
        btn.setOnClickListener {
            procSrc2rectangle()
            txtShow.text = showTxt
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
        val srcBitmap = BitmapFactory.decodeResource(resources, imgPath)
        Utils.bitmapToMat(srcBitmap, srcImg)//convert original bitmap to Mat, R G B.
        val dstImg = srcImg.clone()
        Imgproc.cvtColor(srcImg, srcImg, Imgproc.COLOR_RGB2GRAY)//rgbMat to gray grayMat
        Imgproc.threshold(srcImg, srcImg, 127.0, 255.0, Imgproc.THRESH_BINARY)

        var contours = mutableListOf<MatOfPoint>()
        var hierarcy = Mat()

        Imgproc.findContours(srcImg, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)
        var boundRect = mutableListOf<Rect>()
        var box = mutableListOf<RotatedRect>()
        var rect = arrayOf(Point(), Point(), Point(), Point())

        showTxt = "一共验到有${contours.size}个物体"

        for (i in contours.indices) {
            box.add(Imgproc.minAreaRect(MatOfPoint2f(*contours[i].toArray())))
//            boundRect.add(Imgproc.boundingRect(contours[i]))

            box[i].points(rect)
            if (getLongFrom2Point(rect[1], rect[0]) < 200) {
                continue
            }

            //标记中心点
            Imgproc.circle(dstImg, Point(box[i].center.x, box[i].center.y), 5, Scalar(0.0, 255.0, 0.0), -1, 8)

            //画与xy轴平行的最小外接矩形
            /* Imgproc.rectangle(
                 dstImg, Point(boundRect[i].x.toDouble(), boundRect[i].y.toDouble()),
                 Point(boundRect[i].x + boundRect[i].width.toDouble(), boundRect[i].y + boundRect[i].height.toDouble()),
                 Scalar(0.0, 255.0, 0.0), 2, 8
             )*/
            //画最小外接矩形
            //横向是x轴,纵向是y轴
            for (j in 0..3) {
                Imgproc.line(dstImg, rect[j], rect[(j + 1) % 4], Scalar(0.0, 0.0, 255.0), 2, 8)
            }
            Log.d("area", "\n第$i 个最小外接矩形面积是${areaFrom2Point(rect[1], rect[0], rect[2])}")
            showTxt += "\n第$i 个最小外接矩形面积是${areaFrom2Point(rect[1], rect[0], rect[2])}"
        }
        Utils.matToBitmap(dstImg, srcBitmap) //convert mat to bitmap
        img.setImageBitmap(srcBitmap)

    }

    fun getLongFrom2Point(point1: Point, point2: Point): Double {
        val x1 = point1.x
        val y1 = point1.y
        val x2 = point2.x
        val y2 = point2.y

        return Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)))
    }

    fun areaFrom2Line(line1: Double, line2: Double) = line1 * line2

    fun areaFrom2Point(point1: Point, point12: Point, point13: Point) =
        areaFrom2Line(getLongFrom2Point(point1, point12), getLongFrom2Point(point1, point13))

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}
