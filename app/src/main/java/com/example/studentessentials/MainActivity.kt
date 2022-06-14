package com.example.studentessentials

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import kotlinx.android.synthetic.main.cases_layout.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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

    private lateinit var listOfCases: Array<String>
    private lateinit var logoPwrBMP: Bitmap
    private lateinit var chosenCase: String
    private lateinit var studentsData: String
    private lateinit var currentDate: String
    private lateinit var fileNameToSave: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        eventList = mutableListOf()
        budgetList = mutableListOf()
        budgetExpenses = mutableListOf()
        noteList = mutableListOf()
        listOfCases = arrayOf("Dean's leave", "Resignation from studies", "Advance course")
        logoPwrBMP = BitmapFactory.decodeResource(resources, R.drawable.pwrlogo)
        chosenCase = ""
        currentDate = LocalDate.now().toString()
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
    fun goCases(view: View) {
        setContentView(R.layout.cases_layout)





        btnChooseCase.setOnClickListener{
            val mBuilder = AlertDialog.Builder(this@MainActivity)
            mBuilder.setTitle("Choose an item")
            mBuilder.setSingleChoiceItems(listOfCases, -1) { dialogInterface, i ->
                tvChCase.text = listOfCases[i]
                chosenCase = listOfCases[i]
                dialogInterface.dismiss()
            }
            mBuilder.setNeutralButton("Back"){dialog, which->
                dialog.cancel()
            }
            val mDialog = mBuilder.create()
            mDialog.show()
        }
        btnGenPdf.setOnClickListener{
            if (etCaseStudent.text.toString().isNotEmpty() and chosenCase.isNotEmpty()) {
                studentsData = etCaseStudent.text.toString()
                fileNameToSave = "/$studentsData-$chosenCase-$currentDate.pdf"
                createPdfFile()
                Toast.makeText(this, "File saved as: $fileNameToSave", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Need your data or case", Toast.LENGTH_SHORT).show()}

        }
    }
    fun createPdfFile(){
        val myPaint = Paint()
        val pwrCase = PdfDocument()
        val titlePaint = Paint()
        val caseFile = File(Environment.getExternalStorageDirectory(), fileNameToSave)

        val myPageInfo1 = PdfDocument.PageInfo.Builder(1200,2010, 1).create()
        val myPage1 = pwrCase.startPage(myPageInfo1)
        val canvas = myPage1.canvas
        canvas.drawBitmap(logoPwrBMP, 0f, 0f, myPaint)
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 50f
        canvas.drawText("Application for $chosenCase", 600f, 400f, titlePaint)

        myPaint.textAlign = Paint.Align.RIGHT
        myPaint.textSize = 40f
        myPaint.color = Color.BLACK
        canvas.drawText("Dean of the Faculty", 1180f , 650f, myPaint)
        canvas.drawText("Faculty of Microsystems Electronics and Photonics", 1180f , 695f, myPaint)
        canvas.drawText("Wrocław University of Science and Technology", 1180f , 740f, myPaint)

        canvas.drawText("$currentDate", 1180f, 50f, myPaint)
        canvas.drawText("--------------------------------------",1140f, 1580f, myPaint)
        canvas.drawText("Signature and Date", 1150f, 1608f, myPaint)

        myPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("$studentsData", 20f, 580f, myPaint)
        canvas.drawText("Please consent to the ${chosenCase.lowercase()}", 20f, 1100f, myPaint)


        pwrCase.finishPage(myPage1)
        try{
            with(pwrCase) { writeTo(FileOutputStream(caseFile)) }
        }catch (E: IOException){
            E.printStackTrace()
        }
        pwrCase.close()
    }
}


    data class Expense(val id: Int,val value: Float, val date: String, val expenseType: ExpenseType)

    enum class ExpenseType {
        Food, Transport, Entertainment, Clothes
    }




