package com.example.howold // com.example.howold.MainActivity.kt
import DatabaseHelper
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

// <a href="https://www.flaticon.com/free-icons/cake"
// title="cake icons">Cake icons created by Freepik - Flaticon</a>

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var nameInput: EditText
    private lateinit var dobInput: EditText
    private lateinit var addPersonButton: Button
    private lateinit var ageList: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var addPersonFAB: FloatingActionButton
    private lateinit var addPersonLayout: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val peopleDisplayList = mutableListOf<String>() // Holds the formatted name - age strings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)
        nameInput = findViewById(R.id.nameInput)
        dobInput = findViewById(R.id.dobInput)
        addPersonButton = findViewById(R.id.addPersonButton)
        ageList = findViewById(R.id.ageList)
        addPersonFAB = findViewById(R.id.addPersonFAB)
        addPersonLayout = findViewById(R.id.addPersonLayout)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Initialize ArrayAdapter for the ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, peopleDisplayList)
        ageList.adapter = adapter

        // Load existing data into the ListView
        refreshList()

        // Set up FAB click to show or hide the Add Person fields
        addPersonFAB.setOnClickListener { toggleAddPersonLayout() }

        addPersonButton.setOnClickListener { addPerson() }

        // Set up long-click listener to remove a person
        ageList.setOnItemLongClickListener { _, _, position, _ ->
            val personInfo = peopleDisplayList[position]
            val personName = personInfo.substringBefore(" is ") // Extract the name part
            showDeleteConfirmationDialog(personName)
            true // Indicate that the long-click event was handled
        }

        // Set up SwipeRefreshLayout to refresh the list on swipe
        swipeRefreshLayout.setOnRefreshListener {
            refreshList()
            swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator after refreshing
        }
    }


    private fun toggleAddPersonLayout() {
        if (addPersonLayout.visibility == View.GONE) {
            // Show the layout if it's hidden
            addPersonLayout.visibility = View.VISIBLE
        } else {
            // Hide the layout if it's already visible
            addPersonLayout.visibility = View.GONE
        }
    }

    private fun addPerson() {
        val name = nameInput.text.toString().trim()
        val dob = dobInput.text.toString().trim()

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please enter both name and date of birth", Toast.LENGTH_SHORT).show()
            return
        }

        // Insert the person into the database
        val result = databaseHelper.addPerson(name, dob)

        if (result != -1L) {
            // If insert was successful, show a toast and refresh the list
            Toast.makeText(this, "Person added successfully", Toast.LENGTH_SHORT).show()

            // Clear input fields
            nameInput.setText("")
            dobInput.setText("")
            addPersonLayout.visibility = View.GONE  // Hide the layout after adding
            // Refresh the list of people
            refreshList()
        } else {
            // If insert failed, show an error toast
            Toast.makeText(this, "Failed to add person", Toast.LENGTH_SHORT).show()
        }

    nameInput.setText("")
    dobInput.setText("")
    }

    private fun refreshList() {
        val peopleList = databaseHelper.getAllPeople()
        peopleDisplayList.clear()

        peopleList.forEach { (name, dob) ->
            val ageText = calculateAge(dob)
            peopleDisplayList.add("$name is $ageText")
        }

        adapter.notifyDataSetChanged()
    }

    private fun showDeleteConfirmationDialog(name: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Person")
            .setMessage("Are you sure you want to delete $name?")
            .setPositiveButton("Yes") { _, _ ->
                deletePerson(name)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deletePerson(name: String) {
        val isDeleted = databaseHelper.deletePerson(name)

        if (isDeleted) {
            Toast.makeText(this, "$name has been deleted", Toast.LENGTH_SHORT).show()
            refreshList()
        } else {
            Toast.makeText(this, "Failed to delete $name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateAge(dobString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dob = sdf.parse(dobString)

            val birthDate = Calendar.getInstance()
            val today = Calendar.getInstance()
            birthDate.time = dob as Date

            var years = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
            var months = today.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)

            if (months < 0) {
                years--
                months += 12
            }

            "$years years and $months month(s)"
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format. Use yyyy-mm-dd.",
                Toast.LENGTH_SHORT).show()
            ""
        }
    }
}
