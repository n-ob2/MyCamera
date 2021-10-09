package com.example.mycamera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mycamera.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_PREVIEW = 1 // インテントの為にアプリ内で決めたコード
    val REQUEST_PICTURE = 2 //
    val REQUEST_EXTERNAL_STRAGE = 3 //パーミッション

    lateinit var currentPhotoUri : Uri  //

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId){
                R.id.preview ->
                    binding.cameraButton.text = binding.preview.text
                R.id.takePicture ->
                    binding.cameraButton.text = binding.takePicture.text
            }
        }

        binding.cameraButton.setOnClickListener {
            when (binding.radioGroup.checkedRadioButtonId){
                R.id.preview -> preview()
                R.id.takePicture -> takePicture()
            }
        }   //cameraButton.setOnClickListener ↑↑

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){    //権限の確認ウィンドウを表示
            storagePermission()
        }
    }   //onCreate↑↑

    private fun storagePermission(){
        val permission = ContextCompat.checkSelfPermission(  //パーミッションが許可の是非を戻り値で確認
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED){   //許可されてない場合は
            ActivityCompat.requestPermissions(  //ユーザーに許可を要求
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STRAGE
            )
        }
    }   //fun storagePermission↑↑

    override fun onRequestPermissionsResult(    //ユーザーの権限の許可不許可の選択結果を受け取る（コールバックメソッド）
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){
            REQUEST_EXTERNAL_STRAGE -> {    //不許可ならボタンを無効に
                binding.cameraButton.isEnabled = grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED    //許可ならボタンを有効に
            }
        }
    }

    private fun preview() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{ intent -> //アクションの指定「カメラアプリで画像をキャプチャして返す」
            intent.resolveActivity(packageManager)?.also{   //確実にインテントを渡すnull許容型
                startActivityForResult(intent, REQUEST_PREVIEW) //遷移先を起動してその結果を受け取る
            }
        }
    }   // fun preview ↑↑

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->    //インテントのアクションにACTION_IMAGE_CAPTUREを設定
            intent.resolveActivity(packageManager)?.also {
                val time: String = SimpleDateFormat("yyyyMMdd_HHmmss")  //ファイルの命名規則
                    .format(Date())
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME,"${time}_.jpg") //ファイル名称
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") //ファイル形式
                }
                val collection = MediaStore.Images.Media
                    .getContentUri("external")  //画像メディアを格納するための外部ストレージの場所を取得
                val photoUri = contentResolver.insert(collection, values)   //外部ストレージの場所と値を格納した定数を引数に渡して、最終的な保存先を確保
                photoUri?.let{
                    currentPhotoUri = it    //ファイルの場所を保持
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)  //画像の保存先を指定
                startActivityForResult(intent, REQUEST_PICTURE) //8
            }
        }
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?) { //遷移先アクティビティを閉じると起動
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PREVIEW && resultCode == RESULT_OK) { //どのアクティビティから戻って来たのか判別し、resultCodeには処理の結果が入っている
            val imageBitmap =
                data?.extras?.get("data") as Bitmap   //インテントdataのエクストラdataキーに画像データがあるので、bindingで表示
            binding.imageView.setImageBitmap(imageBitmap)
        }else if(requestCode == REQUEST_PICTURE){
            when(resultCode){
                RESULT_OK -> {
                    Intent(Intent.ACTION_SEND).also { share ->  // インテントのアクション設定
                        share.type = "image/*"  // インテントのタイプ設定
                        share.putExtra(Intent.EXTRA_STREAM, currentPhotoUri)    //画像ファイルの場所をUriで指定
                        startActivity(Intent.createChooser(share, "Share to"))  //アプリ選択画面を表示するインテント
                    }
                }
                else -> {
                    contentResolver.delete(currentPhotoUri,null,null)   //9
                }
            }
        }
    }
}