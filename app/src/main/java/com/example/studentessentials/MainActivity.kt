package com.example.studentessentials

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.budget_layout.*
import kotlinx.android.synthetic.main.budgetchart.*
import kotlinx.android.synthetic.main.calendar_layout.*
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var dateView: TextView
    private lateinit var event: String
    private lateinit var date: String
    private lateinit var evitem: String
    private lateinit var eventList: MutableList<String>

    private lateinit var budgetList: MutableList<String>
    private lateinit var typeBudget: String
    private lateinit var typeBudgetList: List<String>
    private lateinit var valueBudget: String
    private lateinit var valuesBudgetFood: MutableList<Float>
    private lateinit var valuesBudgetTransport: MutableList<Float>
    private lateinit var valuesBudgetClothes: MutableList<Float>
    private lateinit var valuesBudgetEntertainment: MutableList<Float>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        eventList = mutableListOf()
        budgetList = mutableListOf()
        typeBudgetList = listOf("Food", "Transport", "Clothes", "Entertainment")

        valuesBudgetFood = mutableListOf()
        valuesBudgetTransport = mutableListOf()
        valuesBudgetClothes = mutableListOf()
        valuesBudgetEntertainment = mutableListOf()


    }

    fun goCalendar(view: View) {
        setContentView(R.layout.calendar_layout)
        calendarView = findViewById(R.id.cvDate)
        dateView = findViewById(R.id.dateView)
        calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            date = dayOfMonth.toString() + "-" + (month + 1) + "-" + year
            dateView.text = date
        }
        val btnDelEvent = findViewById<Button>(R.id.bttDel)
        lvEvent.setOnItemClickListener { _, _, position, _ ->
            val eventPos = position
            Toast.makeText(this, "Event chosen", Toast.LENGTH_SHORT).show()

        btnDelEvent.setOnClickListener {
            val adapterEvDel: ArrayAdapter<String> = ArrayAdapter(this,
                android.R.layout.simple_list_item_checked, eventList)
            lvEvent.adapter = adapterEvDel
            eventList.removeAt(eventPos)
            adapterEvDel.notifyDataSetChanged()
        }
        }

    }
    fun addEvent(view: View){
        val adapterEvAdd: ArrayAdapter<String> = ArrayAdapter(this,
            android.R.layout.simple_list_item_checked, eventList)
        lvEvent.adapter = adapterEvAdd
        event = etEvent.text.toString()
        if(event.isEmpty()){
            Toast.makeText(this, "No event given", Toast.LENGTH_SHORT).show()
        }
        else {
            evitem = ("$event -> $date")
            adapterEvAdd.add(evitem)
            etEvent.text.clear()
        }

    }

    fun goMenu(view: View) {
        setContentView(R.layout.activity_main)
    }
    fun goBudget(view: View){
        setContentView(R.layout.budget_layout)

        val btnDelBudg = findViewById<Button>(R.id.btnDelBudget)
        val btnAddBudg = findViewById<Button>(R.id.btnAddBudget)

        lvBudget.setOnItemClickListener { _, _, position, _ ->
            val budgPos = position
            Toast.makeText(this, "Expense chosen", Toast.LENGTH_SHORT).show()

            btnDelBudg.setOnClickListener {
                val adapterBudgDel: ArrayAdapter<String> = ArrayAdapter(this,
                    android.R.layout.simple_list_item_checked, budgetList)
                lvBudget.adapter = adapterBudgDel
                budgetList.removeAt(budgPos)
                adapterBudgDel.notifyDataSetChanged()
            }
        }

        btnAddBudg.setOnClickListener{
            val adapterBudgAdd: ArrayAdapter<String> = ArrayAdapter(this,
                android.R.layout.simple_list_item_checked, budgetList)
            lvBudget.adapter = adapterBudgAdd
            valueBudget = etValueBudget.text.toString()
            typeBudget = etTypeBudget.text.toString()
            val budgItem: String
            val currentDateBudg: String


            if(valueBudget.isEmpty() or typeBudget.isEmpty()){
                Toast.makeText(this, "No enough info given", Toast.LENGTH_SHORT).show()
            }
            else if (typeBudget !in typeBudgetList){
                Toast.makeText(this, "Unknown expense, try: Food, Transport, Clothes, Entertainment", Toast.LENGTH_SHORT).show()
            }
            else{
                currentDateBudg = LocalDate.now().toString()
                budgItem = ("$valueBudget zl for $typeBudget on $currentDateBudg")
                val valueToAddBudget = etValueBudget.text.toString().toFloat()
                etTypeBudget.text.clear()
                etValueBudget.text.clear()

                when (typeBudget) {
                    in "Food" -> {
                        valuesBudgetFood.add(valueToAddBudget)
                        adapterBudgAdd.add(budgItem)
                    }
                    in "Clothes" -> {
                        valuesBudgetClothes.add(valueToAddBudget)
                        adapterBudgAdd.add(budgItem)
                    }
                    in "Transport" -> {
                        valuesBudgetEntertainment.add(valueToAddBudget)
                        adapterBudgAdd.add(budgItem)
                    }
                    in "Entertainment" -> {
                        valuesBudgetTransport.add(valueToAddBudget)
                        adapterBudgAdd.add(budgItem)
                    }
                }
            }
        }
    }

    fun generateChart(view: View){
        setContentView(R.layout.budgetchart)

        val foodSum = valuesBudgetFood.sum()
        val clothesSum = valuesBudgetClothes.sum()
        val enterSum = valuesBudgetEntertainment.sum()
        val transportSum = valuesBudgetTransport.sum()


        val barBudgetList = ArrayList<BarEntry>()
        val barBudgetList1 = ArrayList<BarEntry>()
        val barBudgetList2 = ArrayList<BarEntry>()
        val barBudgetList3 = ArrayList<BarEntry>()
        barBudgetList.add(BarEntry(1f, foodSum))
        barBudgetList1.add(BarEntry(2f, clothesSum))
        barBudgetList2.add(BarEntry(3f, enterSum))
        barBudgetList3.add(BarEntry(4f, transportSum))

        val setBudget = BarDataSet(barBudgetList, "Food")
        val setBudget1 = BarDataSet(barBudgetList1, "Clothes")
        val setBudget2 = BarDataSet(barBudgetList2, "Entertainment")
        val setBudget3 = BarDataSet(barBudgetList3, "Transport")

        setBudget.color = Color.BLACK
        setBudget1.color = Color.BLUE
        setBudget2.color = Color.GREEN
        setBudget3.color = Color.RED

        setBudget.valueTextSize = 20f
        setBudget1.valueTextSize = 20f
        setBudget2.valueTextSize = 20f
        setBudget3.valueTextSize = 20f

        val budgetData = BarData(setBudget, setBudget1, setBudget2, setBudget3)

        barBudgetChart.data = budgetData
        barBudgetChart.legend.textSize = 16f
        barBudgetChart.description.isEnabled = false
        barBudgetChart.xAxis.setDrawAxisLine(false)
        barBudgetChart.xAxis.setDrawGridLines(false)
        barBudgetChart.xAxis.setDrawLabels(false)

    }



    fun goCases(view: View){
        setContentView(R.layout.cases_layout)
    }
    fun goDestination(view: View){
        setContentView(R.layout.destination_layout)
    }
    fun goNotes(view: View){
        setContentView(R.layout.notes_layout)
    }
}

