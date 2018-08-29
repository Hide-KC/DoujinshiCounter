package com.kcpoipoi.doujinshicounter

import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.Manifest
import android.preference.PreferenceManager
import android.util.SparseArray
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), CameraFragment.IBarcodeDetectionListener {
    private var isCameraPermitted = false
    private var isStorageAccessPermitted = false
    private var detectedFragment: DetectedBooksFragment? = null
    private var counter = 0
    private var commitCounter = 0
    private val initialCount= 10

    private enum class FragmentTag{
        DETECTED, CAMERA
    }

    private enum class PreferenceKey{
        INITIAL, DETECTED_COUNT //累計検出された部数
    }

    override fun detected(detectedItems: SparseArray<Barcode>) {
        //CameraFragmentは、ゼロのときも常にdetectedItemsを送り続ける。
        //MainActivityは１以上のときフラグを上げて検出し、ゼロになった瞬間フラグを下げて計数する。
        //検出中はダイアログやリストに読み取った書籍を逐次リストアップする。

        if (detectedItems.size() > 0){
            if (detectedFragment == null){
                val fragment = supportFragmentManager.findFragmentByTag(FragmentTag.DETECTED.name)
                detectedFragment = fragment as DetectedBooksFragment
            }

            if (detectedItems.size() > counter){
                detectedFragment?.setDetectedItems(detectedItems)
                for (i in 0 until detectedItems.size()){
                    if (detectedItems.get(detectedItems.keyAt(i)).rawValue == "1"){
                        counter++ //実質１種類のみ対応
                        commitCounter = 0
                        Log.d(javaClass.simpleName, counter.toString())
                    }
                }
            }
        } else if (counter > 0) {
            //検出終了直後にここに入る
            //部数を加（頒布数）・減算（残部数）
            //日時を記録
            //DB保存
            if (commitCounter > 20){
                //ゼロがn回以上入ったらコミット（30fps）
                updateCount()
                Log.d(javaClass.simpleName, counter.toString())

                detectedFragment?.clear()
                detectedFragment = null
                counter = 0
                commitCounter = 0
            } else {
                commitCounter++
            }
        } else {
            detectedItems.clear()
        }
    }

    //AppCompatActibityにこのメソッドがあるのでoverrideする
    // requestCodeはrequestPermissionsの第三引数の数値が入るので、どの処理であるが区別できる
    // 第二引数は許可を求めたデバイス　第三引数には許可／不許可が入る
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                for (i: Int in permissions.indices) {
                    if ((permissions[i] == Manifest.permission.CAMERA) && (grantResults[i] == PackageManager.PERMISSION_GRANTED)) {
                        //許可されてデバイスがここでわかるので何らかの処理をする
                        isCameraPermitted = true
                    }
                }
            }
            2 -> {
                for (i: Int in permissions.indices) {
                    if ((permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) && (grantResults[i] == PackageManager.PERMISSION_GRANTED)) {
                        //許可されてデバイスがここでわかるので何らかの処理をする
                        isStorageAccessPermitted = true
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //許可を得るための自作メソッド
        doCheckCameraPermission()
        doCheckWriteStoragePermission()

        if (isCameraPermitted){
            val cameraFragment = CameraFragment.newInstance(null)
            val cameraTransaction = supportFragmentManager.beginTransaction()
            cameraTransaction.replace(R.id.camera_frame, cameraFragment, FragmentTag.CAMERA.name)
            cameraTransaction.commit()
        }

        val detectedFragment = DetectedBooksFragment.newInstance(null)
        val detectedTransaction = supportFragmentManager.beginTransaction()
        detectedTransaction.replace(R.id.list_frame, detectedFragment, FragmentTag.DETECTED.name)
        detectedTransaction.commit()

        val manager = PreferenceManager.getDefaultSharedPreferences(this)
        reset.setOnClickListener {
            val editor = manager.edit()
            editor.putInt(PreferenceKey.DETECTED_COUNT.name, 0)
            editor.apply()

            sold_count.text = 0.toString()
            left_count.text = initialCount.toString()
        }

        val soldCount = manager.getInt(PreferenceKey.DETECTED_COUNT.name, initialCount)
        val leftCount = initialCount - soldCount
        sold_count.text = soldCount.toString()
        left_count.text = leftCount.toString()
    }

    private fun doCheckCameraPermission(){
        //APIレベルが23未満だと、何もせず実行していい
        if (Build.VERSION.SDK_INT < 23) {
            //ここで何かしらの処理を実行して終了
            this.isCameraPermitted = true
            return
        }

        //すでに許可を得られていたら実行可能
        if (checkPermission(Manifest.permission.CAMERA)) {
            //ここで何かしらの処理を実行して終了
            this.isCameraPermitted = true
            return
        }

        //ダイアログが表示可能であれば使用許可を求めるダイアログを表示して返事を待つ
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            //許可待ちなのでこのメソッドは終了
            return
        }

        //ここまで来てしまった場合には許可もなくダイアログも表示できない状態
        Log.v("nullpo", "(´･ω･｀) ")
    }
    
    private fun doCheckWriteStoragePermission(){
        //APIレベルが23未満だと、何もせず実行していい
        if (Build.VERSION.SDK_INT < 23) {
            //ここで何かしらの処理を実行して終了
            this.isStorageAccessPermitted = true
            return
        }

        //すでに許可を得られていたら実行可能
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //ここで何かしらの処理を実行して終了
            this.isStorageAccessPermitted = true
            return
        }

        //ダイアログが表示可能であれば使用許可を求めるダイアログを表示して返事を待つ
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
            //許可待ちなのでこのメソッドは終了
            return
        }

        //ここまで来てしまった場合には許可もなくダイアログも表示できない状態
        Log.v("nullpo", "(´･ω･｀) ")
    }

    private fun checkPermission(manifestPermission: String): Boolean{
        return ContextCompat.checkSelfPermission(this, manifestPermission) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateCount(){
        val manager = PreferenceManager.getDefaultSharedPreferences(this)
        val soldCount = manager.getInt(PreferenceKey.DETECTED_COUNT.name, 0) + counter
        val initialCount = manager.getInt(PreferenceKey.INITIAL.name, initialCount)
        val leftCount = initialCount - soldCount

        if (leftCount > 0){
            sold_count.text = soldCount.toString()
            left_count.text = leftCount.toString()

            val editor = manager.edit()
            editor.putInt(PreferenceKey.DETECTED_COUNT.name, soldCount)
            editor.apply()
        } else if (leftCount <= 0){
            //CompleteActivity起動
            Log.d(javaClass.simpleName, "Congratulation...!!Congratulation...!!")
        }
    }
}
