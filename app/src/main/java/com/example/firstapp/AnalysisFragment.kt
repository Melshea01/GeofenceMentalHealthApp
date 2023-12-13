package com.example.firstapp

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar



class AnalysisFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var scatterChart: ScatterChart
    private var selectedDate: Long = System.currentTimeMillis()

    private lateinit var xBarChart: BarChart
    private lateinit var yBarChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        //Access to database
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!
        val answerReference = database.reference.child(user.uid).child("Answer")

        //reference xml element that will be updated
        scatterChart= view.findViewById(R.id.graphView)
        val datePickerEditText = view.findViewById<EditText>(R.id.datePickerEditText)
        xBarChart = view.findViewById(R.id.barChartArousal)
        yBarChart = view.findViewById(R.id.barChartValence)

        //Selection of date
        datePickerEditText.setOnClickListener {
            showDatePickerDialog(datePickerEditText)
        }

        answerReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val points = mutableListOf<Entry>()
                val currentTime = System.currentTimeMillis()
                val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000
                val valuesY = mutableListOf<Float>()
                val valuesX = mutableListOf<Float>()
                var count = 0

                for (answerSnapshot in snapshot.children) {
                    val xCoordinateDouble = answerSnapshot.child("x").getValue(Double::class.java)
                    val yCoordinateDouble = answerSnapshot.child("y").getValue(Double::class.java)
                    for (data in answerSnapshot.children) {
                        // Assuming "date" and "value" are child nodes of each "idanswer" node
                        val date = data.child("time").getValue(Long::class.java)


                        if (xCoordinateDouble != null && yCoordinateDouble != null && date != null &&
                            currentTime - date <= sevenDaysInMillis
                        ) {
                            // Convert doubles to floats
                            val xCoordinate = xCoordinateDouble.toFloat()
                            val yCoordinate = yCoordinateDouble.toFloat()

                            // Data for the first graph
                            points.add(Entry(xCoordinate, yCoordinate))
                            valuesY.add(yCoordinate)
                            valuesX.add(xCoordinate)
                            count++
                        }
                    }
                }


                //Scatter Plot

                val scatterDataSet = ScatterDataSet(points, "Points")

                // Customize the appearance of the ScatterDataSet
                val xAxis = scatterChart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(true)
                xAxis.isGranularityEnabled = true
                xAxis.granularity = 0.2f
                xAxis.axisMinimum = -1f
                xAxis.axisMaximum = 1f

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

                // Customize the appearance of the Y-Axis
                val yAxis = scatterChart.axisLeft
                yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                yAxis.setDrawGridLines(true)
                yAxis.axisMinimum = -1f
                yAxis.axisMaximum = 1f
                yAxis.setDrawLabels(true)

                val rightYAxis = scatterChart.axisRight
                rightYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                rightYAxis.setDrawGridLines(true)
                rightYAxis.axisMinimum = -1f
                rightYAxis.axisMaximum = 1f
                rightYAxis.setDrawLabels(false)

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

                val scatterData = ScatterData(scatterDataSet)
                scatterChart.description.isEnabled = false;
                scatterChart.data = scatterData
                scatterChart.invalidate()



                //Barchat 1 for valence
                if (count > 0) {
                    val sumXBelowZero = valuesX.filter { it < 0 }.sum()
                    val countBelowZero = valuesX.count { it < 0 }
                    val averageBelowZero = if (countBelowZero > 0) sumXBelowZero / countBelowZero else 0f

                    val sumXAboveZero = valuesX.filter { it > 0 }.sum()
                    val countAboveZero = valuesX.count { it > 0 }
                    val averageAboveZero = if (countAboveZero > 0) sumXAboveZero / countAboveZero else 0f

                    val sumX = valuesX.sum()
                    val overallAverage = sumX / count

                    val xBarEntries = mutableListOf<BarEntry>()
                    xBarEntries.add(BarEntry(0f, averageBelowZero))
                    xBarEntries.add(BarEntry(1f, overallAverage))
                    xBarEntries.add(BarEntry(2f, averageAboveZero))

                    val xBarDataSet = BarDataSet(xBarEntries, "Average Valence")
                    // Nuances de vert pastel
                    val pastelGreen1 = Color.rgb(144, 238, 144)  // Vert pastel clair
                    val pastelGreen2 = Color.rgb(152, 251, 152)  // Vert pastel moyen
                    val pastelGreen3 = Color.rgb(173, 255, 173)  // Vert pastel foncé

                    // Utilisez ces nuances de vert pastel pour votre BarDataSet
                    xBarDataSet.colors = mutableListOf(pastelGreen1, pastelGreen2, pastelGreen3)

                    val xBarData = BarData(xBarDataSet)

                    // Customize appearance of the BarChart
                    val xAxisValence = xBarChart.xAxis
                    xAxisValence.position = XAxis.XAxisPosition.BOTTOM
                    xAxisValence.setDrawGridLines(false)

                    val yAxisValence = xBarChart.axisLeft
                    yAxisValence.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    yAxisValence.setDrawGridLines(false)

                    xBarChart.data = xBarData
                    xBarChart.invalidate()
                }

                //Barchat 2 for arousal
                if (count > 0) {
                    val sumYBelowZero = valuesY.filter { it < 0 }.sum()
                    val countYBelowZero = valuesY.count { it < 0 }
                    val averageYBelowZero = if (countYBelowZero > 0) sumYBelowZero / countYBelowZero else 0f

                    val sumYAboveZero = valuesY.filter { it > 0 }.sum()
                    val countYAboveZero = valuesY.count { it > 0 }
                    val averageYAboveZero = if (countYAboveZero > 0) sumYAboveZero / countYAboveZero else 0f

                    val sumY = valuesY.sum()
                    val overallAverageY = sumY / count

                    val yBarEntries = mutableListOf<BarEntry>()
                    yBarEntries.add(BarEntry(0f, averageYBelowZero))
                    yBarEntries.add(BarEntry(1f, overallAverageY))
                    yBarEntries.add(BarEntry(2f, averageYAboveZero))

                    val yBarDataSet = BarDataSet(yBarEntries, "Average Arousal")
                    yBarDataSet.colors = mutableListOf(Color.RED, Color.BLUE, Color.GREEN)

                    val yBarData = BarData(yBarDataSet)

                    // Customize appearance of the BarChart
                    val xAxisArousal = yBarChart.xAxis
                    xAxisArousal.position = XAxis.XAxisPosition.BOTTOM
                    xAxisArousal.setDrawGridLines(false)

                    val yAxisArousal = yBarChart.axisLeft
                    yAxisArousal.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    yAxisArousal.setDrawGridLines(false)

                    yBarChart.data = yBarData
                    yBarChart.invalidate()
                }



            }



            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("Firebase", "Failed to read value.", error.toException())
            }
        })

        return view
    }

    // Method used when android:onClick="showDatePickerDialog" is clicked

    fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = selectedCalendar.timeInMillis

                // Update the EditText with the selected date
                val selectedDateString = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                (view as EditText).setText(selectedDateString)

            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }


}