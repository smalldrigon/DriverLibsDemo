package com.grgbanking.driverlibsdemo

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Toast

 import com.grgbanking.huitong.driver_libs.card_reader.Driver_DeCardReaderImpl
import com.grgbanking.huitong.driver_libs.fingerprints.Driver_FingerRecongnitionImpl
import com.grgbanking.huitong.driver_libs.interfaces.IDriver_CardReader
import com.grgbanking.huitong.driver_libs.interfaces.IDriver_FingerPrints
import com.grgbanking.huitong.driver_libs.interfaces.IDriver_ScanGun
import com.grgbanking.huitong.driver_libs.scan_qr_code.Driver_ScanQrCodeImpl
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main1.*

class MainActivity : AppCompatActivity() {
    var disposedCopyfIle: Disposable? = null
    var mIDriver_CardReader: IDriver_CardReader? = null
    var mIDriver_FingerPrints: IDriver_FingerPrints? = null
    var mIDriver_ScanGun: IDriver_ScanGun? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)
        initData()
    }

    fun setText(str: String) {
       runOnUiThread{
           tv_resulr.text = "结果$str"
       }
    }




    fun initData() {

        mIDriver_CardReader = Driver_DeCardReaderImpl(this)
//        mIDriver_CardReader?.open(this)
        mIDriver_FingerPrints = Driver_FingerRecongnitionImpl()
        mIDriver_FingerPrints?.open(this)
        mIDriver_ScanGun = Driver_ScanQrCodeImpl()

//
//        DriverManagers.Builder().setContext(this)
//            .setFingerPrints(DriverManagers.FINGERPRINTS_TYPE_FPC1011)
//            .setScanGun(DriverManagers.SACNGUN_TYPE_ZD7100)
//            .setCardReader(DriverManagers.CARD_READER_TYPE_T10)
//            .setGateMachine(DriverManagers.GATEMACHINE_TYPE_M810)
//             .build()







//        try {
//            mIDriver_CardReader?.open(this)
//        }catch (e:Exception){
//            Toast.makeText(this,"读卡器打开失败",Toast.LENGTH_LONG).show()
//        }
        try {
            mIDriver_FingerPrints?.open(this)
        }catch (e:Exception){
            Toast.makeText(this,"指纹仪打开失败",Toast.LENGTH_LONG).show()
        }
//        try {
//            mIDriver_ScanGun?.open(this)
//        }catch (e:Exception){
//            Toast.makeText(this,"扫码枪打开失败",Toast.LENGTH_LONG).show()
//        }

        initView()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        println("${event}")
          mIDriver_ScanGun?.startScan(event
          ) { usbInputType, barcode ->
              when(usbInputType){
                  Driver_ScanQrCodeImpl.INPUT_RQCODE ->{
                      if (barcode != null) setText("扫码：$barcode")
                  }
                  Driver_ScanQrCodeImpl.INPUT_ICCARD->{
                      if (barcode != null) setText("读ic卡：$barcode")
                  }
              }
          }
        return false
    }

    fun initView() {

        btn_readIdCard.setOnClickListener {
            mIDriver_CardReader?.open(this)
            mIDriver_CardReader?.readIdCard(false, 2000L) {

                if (it != null) setText("读取身份证：${it.name}")
            }
        }
        btn_readfinger.setOnClickListener {
            mIDriver_FingerPrints?.getFeature(
                false,
                1000L,
                object : IDriver_FingerPrints.FingerCallBack {
                    override fun fingerResultFeature(feature: ByteArray?) {
                        if (feature != null) setText("识别指纹：${feature.toString()}")
                    }

                    override fun fingerResultBitmap(bitmap: Bitmap?) {
                     }

                })

        }

        btn_gatethree.setOnClickListener {
            startActivity(Intent(this@MainActivity, ThreeRollerGatesActivity::class.java))
        }
        btn_gatebaizha.setOnClickListener {
            startActivity(Intent(this@MainActivity, SluiceGatesActivity::class.java))
        }
        btn_getframe.setOnClickListener {
//            startActivity(Intent(this@MainActivityRecord, MainMenuActivity::class.java))
//            startActivity(Intent(this@MainActivityRecord, GetFrameActivity::class.java))
//            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
//            startActivity(Intent(this@MainActivity2, DemoMainActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()


    }


}
