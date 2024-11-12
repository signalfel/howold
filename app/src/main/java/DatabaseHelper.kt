// DatabaseHelper.kt
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PeopleDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "People"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DOB = "dob"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            val createTable = ("CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_NAME TEXT," +
                    "$COLUMN_DOB TEXT)")
            db.execSQL(createTable)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating database: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert a new person into the database
    fun addPerson(name: String, dob: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_DOB, dob)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result
    }
    fun deletePerson(name: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_NAME = ?", arrayOf(name))
        db.close()
        return result > 0
    }

    // Retrieve all people from the database
    fun getAllPeople(): List<Pair<String, String>> {
        val peopleList = mutableListOf<Pair<String, String>>()
        val selectQuery = "SELECT $COLUMN_NAME, $COLUMN_DOB FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val dob = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB))
                peopleList.add(Pair(name, dob))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return peopleList
    }
}