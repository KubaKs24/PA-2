package com.example.studentessentials

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.android.synthetic.main.budget_layout.*
import kotlinx.android.synthetic.main.budgetchart.*
import kotlinx.android.synthetic.main.calendar_layout.*
import kotlinx.android.synthetic.main.notes_layout.*
import java.time.LocalDate
import kotlin.random.Random
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule


open class MainActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var dateView: TextView
    private lateinit var event: String
    private lateinit var date: String
    private lateinit var evitem: String
    private lateinit var eventList: MutableList<String>
    private lateinit var adapterEvent: ArrayAdapter<String>

    private lateinit var adapterBudget: ArrayAdapter<String>
    private lateinit var budgetList: MutableList<String>
    private lateinit var valueBudget: String
    private lateinit var budgetExpenses: MutableList<Expense>
    private lateinit var adapterNote: NoteAdapter
    private lateinit var noteList: MutableList<Note>
    private lateinit var noteTitle: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        eventList = mutableListOf()
        budgetList = mutableListOf()
        budgetExpenses = mutableListOf()
        noteList = mutableListOf()

    }

    fun goCalendar(view: View) {
        setContentView(R.layout.calendar_layout)

        adapterEvent = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_checked, eventList
        )
        lvEvent.adapter = adapterEvent
        adapterEvent.notifyDataSetChanged()

        calendarView = findViewById(R.id.cvDate)
        dateView = findViewById(R.id.dateView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            date = dayOfMonth.toString() + "-" + (month + 1) + "-" + year
            dateView.text = date
        }
        val btnDelEvent = findViewById<Button>(R.id.bttDel)
        lvEvent.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "Event chosen", Toast.LENGTH_SHORT).show()

            btnDelEvent.setOnClickListener {
                eventList.removeAt(position)
                adapterEvent.notifyDataSetChanged()
            }
        }

    }

    fun addEvent(view: View) {

        event = etEvent.text.toString()
        if (event.isEmpty()) {
            Toast.makeText(this, "No event given", Toast.LENGTH_SHORT).show()
        } else {
            evitem = ("$event -> $date")
            adapterEvent.add(evitem)
            etEvent.text.clear()
        }

    }

    fun goMenu(view: View) {
        setContentView(R.layout.activity_main)
    }

    fun goBudget(view: View) {
        setContentView(R.layout.budget_layout)
        var counter = 0


        adapterBudget = ArrayAdapter(this, android.R.layout.simple_list_item_checked, budgetList)
        lvBudget.adapter = adapterBudget
        adapterBudget.notifyDataSetChanged()

        val btnDelBudg = findViewById<Button>(R.id.btnDelBudget)
        val btnAddBudg = findViewById<Button>(R.id.btnAddBudget)

        btnAddBudg.setOnClickListener {
            valueBudget = etValueBudget.text.toString()
            val typeBudgetFromUser = etTypeBudget.text.toString()
            val typeBudget: ExpenseType? = ExpenseType.values()
                .firstOrNull { it.name == typeBudgetFromUser }
            val budgItem: String
            val currentDateBudg: String


            when {
                valueBudget.isEmpty() -> {
                    Toast.makeText(this, "No enough info given", Toast.LENGTH_SHORT).show()
                }
                typeBudgetFromUser !in ExpenseType.values().map { it.name } -> {
                    Toast.makeText(
                        this,
                        "Unknown expense, try: Food, Transport, Clothes, Entertainment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    currentDateBudg = LocalDate.now().toString()
                    budgItem = ("$valueBudget zl for $typeBudget on $currentDateBudg")

                    try {
                        val valueToAddBudget = etValueBudget.text.toString().toFloat()

                        typeBudget?.let {
                            val budgetExToAdd = Expense(
                                counter,
                                valueToAddBudget,
                                currentDateBudg,
                                it
                            )
                            budgetExpenses.add(
                                budgetExToAdd
                            )
                            adapterBudget.add(budgItem)
                        }
                        counter++
                        Toast.makeText(this, budgetExpenses.size.toString(), Toast.LENGTH_SHORT)
                            .show()

                    } catch (nfe: NumberFormatException) {
                        Toast.makeText(this, "Price need to be number!!!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    etTypeBudget.text.clear()
                    etValueBudget.text.clear()

                }
            }
        }
        lvBudget.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, " Expense chosen", Toast.LENGTH_SHORT).show()

            btnDelBudg.setOnClickListener {

                budgetList.removeAt(position)
                budgetExpenses.removeAt(position)
                adapterBudget.notifyDataSetChanged()
            }
        }
    }

    fun generateChart(view: View) {
        setContentView(R.layout.budgetchart)
        val rnd = Random(3)

        var xVal = 1
        val setBudgetList = mutableListOf<BarDataSet>()

        for (t in ExpenseType.values()) {
            val amount =
                budgetExpenses.filter { it.expenseType == t }.map { it.value }.sum()

            val barBudgetList = ArrayList<BarEntry>()
            barBudgetList.add(BarEntry(xVal.toFloat(), amount))
            val setBudget = BarDataSet(barBudgetList, t.name)
            setBudget.color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            setBudget.valueTextSize = 20f
            xVal++
            setBudgetList.add(setBudget)
        }

        val budgetData = BarData(setBudgetList as List<IBarDataSet>?)

        barBudgetChart.data = budgetData
        barBudgetChart.legend.textSize = 80f / xVal
        barBudgetChart.legend.horizontalAlignment
        barBudgetChart.description.isEnabled = false
        barBudgetChart.xAxis.setDrawAxisLine(false)
        barBudgetChart.xAxis.setDrawGridLines(false)
        barBudgetChart.xAxis.setDrawLabels(false)

    }


    fun goCases(view: View) {
        setContentView(R.layout.cases_layout)
    }

    fun goDestination(view: View) {
        setContentView(R.layout.destination_layout)
    }
    val imagePickerActivityResult = registerForActivityResult(
        StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result != null) {
                    val imageUri = result.getData()?.getData()!!
                    val note = Note(noteTitle, imageUri = imageUri)
                    adapterNote.addNote(note)
                }
            }
        }
    )

    fun goNotes(view: View) {
        setContentView(R.layout.notes_layout)

        adapterNote = NoteAdapter(noteList)
        rvNotes.adapter = adapterNote
        rvNotes.layoutManager = LinearLayoutManager(this)

        btnNoteAdd.setOnClickListener {

            noteTitle = etNoteEnter.text.toString()
            if (noteTitle.isNotEmpty()) {
                val galleryIntent = Intent(Intent.ACTION_PICK)
                galleryIntent.type = "image/*"
                imagePickerActivityResult.launch(galleryIntent)
                etNoteEnter.text.clear()
            } else {
                Toast.makeText(this, "Please enter note", Toast.LENGTH_SHORT).show()
            }
        }
        btnNoteDel.setOnClickListener {
            adapterNote.deleteNote()
        }
    }
}
    data class Expense(val id: Int,val value: Float, val date: String, val expenseType: ExpenseType)

    enum class ExpenseType {
        Food, Transport, Entertainment, Clothes
    }




