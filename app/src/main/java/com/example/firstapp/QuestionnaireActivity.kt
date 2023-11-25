package com.example.firstapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.firstapp.databinding.ActivityMainBinding


class QuestionnaireActivity : FragmentActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var myImageView: ImageView
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var paint: Paint? = null


    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupérer les références aux vues
        myImageView = binding.myImageView
        val submitButton = binding.submitButton


        myImageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x
                    val y = event.y

                    //Move the place of the point
                    drawPoint(x, y)

                    // Testing
                    //Toast.makeText(this, "Clicked at: $x, $y", Toast.LENGTH_SHORT).show()

                    val centerX = myImageView.drawable.intrinsicWidth / 2.0f
                    val centerY = myImageView.drawable.intrinsicHeight / 2.0f
                    val corr_X = (x - centerX)
                    val corr_Y = (centerY - y)

                    // Testing
                    //Toast.makeText(this, "corrected click at: $corr_X, $corr_Y", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        submitButton.setOnClickListener {
            // redirige vers l'activité principal la class MainActivity

            //TODO : save information into the database

            // Provide feedback to the user (optional)
            Toast.makeText(this, "Results saved to the database", Toast.LENGTH_SHORT).show()
        }




    }

    private fun drawPoint(x: Float, y: Float) {
        // Initialize bitmap and canvas if not already initialized
        if (bitmap == null) {
            // Load your background image here
            val backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.empty_grid)

            // Create a new bitmap with the background image
            bitmap = Bitmap.createBitmap(myImageView.width, myImageView.height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap!!)

            // Draw the background image using Matrix to maintain its original size
            val matrix = Matrix()
            matrix.setRectToRect(
                RectF(0f, 0f, backgroundBitmap.width.toFloat(), backgroundBitmap.height.toFloat()),
                RectF(0f, 0f, myImageView.width.toFloat(), myImageView.height.toFloat()),
                Matrix.ScaleToFit.CENTER
            )
            canvas?.drawBitmap(backgroundBitmap, matrix, null)

            myImageView.setImageBitmap(bitmap)
        } else {
            // Clear the canvas to remove previous points and set the new one
            canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            val backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.empty_grid)

            // Create a new bitmap with the background image
            bitmap = Bitmap.createBitmap(myImageView.width, myImageView.height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap!!)

            // Draw the background image using Matrix to maintain its original size
            val matrix = Matrix()
            matrix.setRectToRect(
                RectF(0f, 0f, backgroundBitmap.width.toFloat(), backgroundBitmap.height.toFloat()),
                RectF(0f, 0f, myImageView.width.toFloat(), myImageView.height.toFloat()),
                Matrix.ScaleToFit.CENTER
            )
            canvas?.drawBitmap(backgroundBitmap, matrix, null)

            myImageView.setImageBitmap(bitmap)
        }

        // Initialize the paint if not already done
        if (paint == null) {
            paint = Paint()
            paint!!.color = Color.RED
            paint!!.strokeWidth = 20f // Increase the size of the point
            paint!!.strokeCap = Paint.Cap.ROUND // Make the point round
        }

        // Get the position of the point relative to the displayed image in myImageView
        val scaledX = x * myImageView.drawable.intrinsicWidth / myImageView.width
        val scaledY = y * myImageView.drawable.intrinsicHeight / myImageView.height

        // Draw the point on the canvas
        canvas?.drawPoint(scaledX, scaledY, paint!!)

        // Refresh the ImageView to display the point
        myImageView.invalidate()
    }

}