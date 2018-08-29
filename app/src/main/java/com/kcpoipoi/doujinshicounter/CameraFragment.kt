package com.kcpoipoi.doujinshicounter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.SparseArray
import android.view.*
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class CameraFragment: Fragment() {
    private lateinit var surfaceView: SurfaceView
    private var cameraSource: CameraSource? = null
    private var surfaceCreated = false
    private val defCameraFacing = CameraFacing.FACING_FRONT
    private var listener: IBarcodeDetectionListener? = null
    private var handler: Handler? = null

    enum class CameraFacing(val rawValue: Int){
        FACING_BACK(0), FACING_FRONT(1)
    }

    interface IBarcodeDetectionListener{
        fun detected(detectedItems: SparseArray<Barcode>)
    }

    companion object {
        fun newInstance(targetFragment: Fragment?): CameraFragment{
            val fragment = CameraFragment()
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is IBarcodeDetectionListener){
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        surfaceView = view.findViewById(R.id.camera_preview)
        surfaceView.holder.addCallback(SurfaceCallback())

        return view
    }

    override fun onResume() {
        super.onResume()

        // CameraSourceが未作成 or 破棄されていたら作成
        if (cameraSource == null){
            setupCameraSource(defCameraFacing)
        }

        // SurfaceViewが準備できていたらキャプチャを開始
        if (surfaceCreated){
            startCameraSource(surfaceView.holder)
        }
    }

    override fun onPause() {
        super.onPause()
        if (cameraSource != null){
            //キャプチャ停止
            cameraSource?.stop()
        }
    }

    private fun setupCameraSource(cameraFacing: CameraFacing){
        // QRコードを認識させるためのBarcodeDetectorを作成
        val barcodeDetector = BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build()
        // DetectorにProcessorというコールバックを設定
        barcodeDetector.setProcessor(MyDetectorProcessor())
        // CameraSourceを作成
        cameraSource = CameraSource.Builder(context, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(cameraFacing.rawValue)
                .build()
    }

    private fun startCameraSource(holder: SurfaceHolder){
        try {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                if (handler == null){
                    handler = Handler()
                }
                cameraSource?.start(holder)
            }
        } catch (e: IOException){
            Log.d(javaClass.simpleName, e.message)
        }
    }

    fun setCameraFacing(cameraFacing: CameraFacing){
        if (cameraSource?.cameraFacing != cameraFacing.rawValue){
            cameraSource?.stop()
            cameraSource?.release()
            setupCameraSource(cameraFacing)
        }
    }

    inner class MyDetectorProcessor: Detector.Processor<Barcode>{
        override fun release() { }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            //認識された結果がdetectionsに入ってくる
            if (detections != null){
                handler?.post{
                    listener?.detected(detections.detectedItems)
                }
            } else {
                throw IllegalStateException() //たぶん到達しない
            }
        }
    }

    inner class SurfaceCallback: SurfaceHolder.Callback{
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            if (cameraSource != null){
                cameraSource?.stop()
                cameraSource?.release()
                cameraSource = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun surfaceCreated(holder: SurfaceHolder?) {
            try {
                if (holder != null && permissionCheck()) startCameraSource(holder)
            } catch (e: IOException){
                Log.d(javaClass.simpleName, e.message)
            }
        }

        private fun permissionCheck(): Boolean{
            return ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }
    }


}