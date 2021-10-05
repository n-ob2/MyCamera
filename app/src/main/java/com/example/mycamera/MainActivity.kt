package com.example.mycamera

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.mycamera.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val REQUEST_PREVIEW = 1 // インテントの為にアプリ内で決めたコード

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
    }   //onCreate↑↑

    private fun preview() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{ intent -> //アクションの指定「カメラアプリで画像をキャプチャして返す」
            intent.resolveActivity(packageManager)?.also{   //確実にインテントを渡すnull許容型
                startActivityForResult(intent, REQUEST_PREVIEW) //遷移先を起動してその結果を受け取る
            }
        }
    }   // fun preview ↑↑

    private fun takePicture() {}

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?) { //遷移先アクティビティを閉じると起動
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PREVIEW && resultCode == RESULT_OK){ //どのアクティビティから戻って来たのか判別し、resultCodeには処理の結果が入っている
            val imageBitmap = data?.extras?.get("data") as Bitmap   //インテントdataのエクストラdataキーに画像データがあるので、bindingで表示
            binding.imageView.setImageBitmap(imageBitmap)
        }
    }
}