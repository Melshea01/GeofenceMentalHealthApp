package com.example.firstapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.firstapp.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Date
import java.util.UUID


class QuestionnaireActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser
    val database = FirebaseDatabase.getInstance()

    private lateinit var scatterChart: ScatterChart
    private var userPoints = ArrayList<Entry>()

    private var xVal: Float = -2.0f
    private var yVal: Float = -2.0f


    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scatterChart = findViewById(R.id.scatterChart)
        val submitButton = binding.submitButton


        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!
        val answerReference = database.reference.child(user.uid)

        //Design of the scatterplot
        val entries: List<Entry> = ArrayList()

        // Créer un ensemble de données de dispersion avec des points vides
        val dataSet = ScatterDataSet(entries, "Data Set 1")
        dataSet.color = Color.TRANSPARENT // Rendre le set de données transparent


        val scatterData = ScatterData(dataSet)
        scatterChart.data = scatterData


        // Personnaliser l'axe X (valence)

        val xAxis = scatterChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 0.2f // Définir la graduation
        xAxis.axisMinimum = -1f // Définir la valeur minimale pour l'axe X
        xAxis.axisMaximum = 1f // Définir la valeur maximale pour l'axe X


        //Line x=0
        val limitLine = LimitLine(0f)
        limitLine.lineWidth = 2f
        limitLine.lineColor = Color.BLACK
        limitLine.label = "High Emotion"
        limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP // Set position to RIGHT_TOP or LEFT_TOP
        xAxis.addLimitLine(limitLine)

        val limitLine2 = LimitLine(0f)
        limitLine2.lineWidth = 2f
        limitLine2.lineColor = Color.BLACK
        limitLine2.label = "Low Emotion"
        limitLine2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM // Set position to RIGHT_TOP or LEFT_TOP
        xAxis.addLimitLine(limitLine2)

        val yAxis = scatterChart.axisLeft
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = -1f
        yAxis.axisMaximum = 1f

        //Line y=0
        val limitLineY = LimitLine(0f)
        limitLineY.lineWidth = 2f
        limitLineY.label = "Negative"
        limitLineY.lineColor = Color.BLACK
        limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        yAxis.addLimitLine(limitLineY)

        val limitLineY2 = LimitLine(0f)
        limitLineY2.lineWidth = 2f
        limitLineY2.label = "Positive"
        limitLineY2.lineColor = Color.BLACK
        limitLineY2.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        yAxis.addLimitLine(limitLineY2)

        // Désactiver l'axe Y à droite
        scatterChart.axisRight.isEnabled = false

        // Mettez à jour le graphique
        scatterChart.description.isEnabled = false
        scatterChart.invalidate()

        scatterChart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
                // Vous pouvez ajouter votre logique ici lorsque le geste commence
            }

            override fun onChartGestureEnd(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
                // Vous pouvez ajouter votre logique ici lorsque le geste se termine
            }

            override fun onChartLongPressed(me: MotionEvent?) {
                // Vous pouvez ajouter votre logique ici lorsque le graphique est longuement pressé
            }

            override fun onChartDoubleTapped(me: MotionEvent?) {
                // Vous pouvez ajouter votre logique ici lorsque le graphique est double-cliqué
            }

            override fun onChartSingleTapped(me: MotionEvent?) {
                // Obtenez les coordonnées du point touché
                val pos = scatterChart.getValuesByTouchPoint(me!!.x, me.y, YAxis.AxisDependency.LEFT)

                // Coordinate x and y of the point
                xVal = pos.x.toFloat()
                yVal = pos.y.toFloat()


                // add the point on the plot
                addPoint(xVal, yVal)

            }

            override fun onChartFling(
                me1: MotionEvent?,
                me2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) {

            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                // Vous pouvez ajouter votre logique ici lorsque le graphique est mis à l'échelle
            }

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                // Vous pouvez ajouter votre logique ici lorsque le graphique est déplacé
            }
        }



        submitButton.setOnClickListener {
            if (xVal != -2.0f && yVal != -2.0f) {
                // Retrieve current date
                val currentDate = Date()
                val answerId = answerReference.push().key ?: UUID.randomUUID().toString()

                // Create Answer object
                val answer = Answer(
                    id = answerId,
                    x = xVal,
                    y = yVal,
                    date = currentDate
                )

                addAnswertoDatabase(answer, answerReference)
                // Provide feedback to the user (optional)
                Toast.makeText(this, "Results saved to the database", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            } else {
                // Handle the case when corr_X or corr_Y is null
                Toast.makeText(this, "Invalid data. Please try again.", Toast.LENGTH_SHORT).show()
            }

        }

    }


    private fun addPoint(x: Float, y: Float) {
        userPoints.clear()
        userPoints.add(Entry(x, y))

        val userDataSet = ScatterDataSet(userPoints, "user")
        userDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        userDataSet.color = Color.RED
        userDataSet.setDrawValues(false)

        val scatterData = ScatterData(userDataSet)
        scatterChart.data = scatterData
        scatterChart.invalidate()

    }

    private fun addAnswertoDatabase(answer:Answer , firebaseReference: DatabaseReference){

        //Handle the network issue
        user?.let { firebaseReference.child("Answer").child(answer.id).setValue(answer) }
            ?: Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
    }

}

data class Answer(
    val id: String,
    val x: Float,
    val y: Float,
    val date: Date
)