package com.kcpoipoi.doujinshicounter

import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.Manifest
import android.util.SparseArray
import com.google.android.gms.vision.barcode.Barcode

class MainActivity : AppCompatActivity(), CameraFragment.IBarcodeDetectionListener {
    private var mGetPermission = false

    override fun detected(detectedItems: SparseArray<Barcode>) {
        //CameraFragmentは、ゼロのときも常にdetectedItemsを送り続ける。
        //MainActivityは１以上のときフラグを上げて検出し、ゼロになった瞬間フラグを下げて計数する。
        //検出中はダイアログやリストに読み取った書籍を逐次リストアップする。

        if (detectedItems.size() > 0){

        } else {

        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                        mGetPermission = true
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //許可を得るための自作メソッド
        doCheckPermission()

        if (mGetPermission){
            val fragment = CameraFragment.newInstance(null)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.camera_frame, fragment)
            transaction.commit()
        }
    }

    private fun doCheckPermission(){
        //APIレベルが23未満だと、何もせず実行していい
        if (Build.VERSION.SDK_INT < 23) {
            //ここで何かしらの処理を実行して終了
            this.mGetPermission = true
            return
        }

        //すでに許可を得られていたら実行可能
        if (checkPermission()) {
            //ここで何かしらの処理を実行して終了
            this.mGetPermission = true
            return
        }

        //ダイアログが表示可能であれば使用許可を求めるダイアログを表示して返事を待つ
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            //許可待ちなのでこのメソッドは終了
            return
        }

        //ここまで来てしまった場合には許可もなくダイアログも表示できない状態
        Log.v("nullpo", "(´･ω･｀) ")
    }

    private fun checkPermission(): Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}
