package com.example.aviaryquest

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.aviaryquest.ml.BirdsModel
import org.tensorflow.lite.support.image.TensorImage
import com.example.aviaryquest.databinding.ActivitySaveBirdsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException

class SaveBirdsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaveBirdsBinding
    private lateinit var imageView : ImageView
    private lateinit var button : Button
    private lateinit var tvOutput: TextView
    private val GALLERY_REQUEST_CODE = 123
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBirdsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        bottomNavigation = findViewById(R.id.bottom_image_observation)
        imageView = binding.imageView
        button = binding .btnCaptueImage
        tvOutput = binding.tvOutput

        val buttonLoad = binding.btnLoadImage
        button.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                takePicturePreview.launch(null)
            }else{
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            }else{
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        //the textview will search on Google
        tvOutput.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${tvOutput.text}"))
            startActivity(intent)
        }

        //function to download the image after being loaded
        imageView.setOnLongClickListener{
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@setOnLongClickListener true
        }

        // Set the bottom navigation view to be selected
        bottomNavigation.selectedItemId = R.id.SavedBirds

        // force the bottom navigation bar to always show icon and title
        bottomNavigation.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

        // Set click listener for bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_Home -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.nav_observations -> {
                    startActivity(Intent(this, SaveObservation::class.java))
                    true
                }
                R.id.SavedBirds -> {
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    //request camera permission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { granted ->
        if (granted) {
            takePicturePreview.launch(null)

        } else {
            Toast.makeText(this, "Permission Denied!! Try Again", Toast.LENGTH_SHORT).show()
        }
    }

    //Launch camera permission
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview())
    { bitmap ->
        if(bitmap != null){
            imageView.setImageBitmap(bitmap)
            outputGenerator(bitmap)
        }

    }

    //to get image from gallery
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        Log.d("TAG", "This is the result: ${result.data} ${result.resultCode}")
        onResultRecieved(GALLERY_REQUEST_CODE, result)
    }

    private fun onResultRecieved(requestCode : Int, result : ActivityResult?){
        when(requestCode){
            GALLERY_REQUEST_CODE ->{
                if(result?.resultCode == Activity.RESULT_OK){
                    result.data?.data?.let{uri ->
                        Log.i("TAG","onResultRecieved: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                }else{
                    Log.e("TAG","onActivityResult: error in selecting image")
                }
            }
        }
    }

    private fun outputGenerator(bitmap: Bitmap){
        //declaring tensor flow lite model variable
        val birdsModel = BirdsModel.newInstance(this)

        //converting bitmap into tensor flow image
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true)
        val tfimage = TensorImage.fromBitmap(newBitmap)


        //process the image using trained model and sort it in descending order
        val outputs = birdsModel.process(tfimage).probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }
        //getting result having high probability
        val highProbabilityOutput = outputs[0]

        //setting output text
        tvOutput.text = highProbabilityOutput.label
        Log.i("TAG","OutputGenerator: $highProbabilityOutput")

    }

    //to download image to device
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
        if(isGranted) {
            AlertDialog.Builder(this).setTitle("Download Image?")
                .setMessage("Do you want to download this image to your device?")
                .setPositiveButton("Yes") { _, _ ->
                    val drawable: BitmapDrawable = imageView.drawable as BitmapDrawable
                    val bitmap = drawable.bitmap;
                    downloadImage(bitmap)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }else{
            Toast.makeText(this, "Please allow permission to download image", Toast.LENGTH_SHORT ).show()
        }
    }

    //fun that takes a bitmap and store to users device
    private fun downloadImage(mBitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Birds_Images" + System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (uri != null) {
            contentResolver.insert(uri, contentValues)?.also { insertUri ->
                contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                    if (!mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        throw IOException("Couldn't save the bitmap")
                    } else {
                        Toast.makeText(applicationContext, "Image Saved", Toast.LENGTH_SHORT).show()
                    }
                }
                return insertUri
            }
        }
        return null
    }

}