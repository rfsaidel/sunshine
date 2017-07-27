/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            Log.v("r.saidel","> "+columnName);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public long testLocationTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues location = new ContentValues();
        location.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,"95050");
        location.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME,"São Francisco");
        location.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT,64);
        location.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG,-147);
        // (you can use the createNorthPoleLocationValues if you wish)

        // Insert ContentValues into database and get a row ID back

        long row = db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,location);
        System.out.println("[rfsaidel] row: "+row);
        // Query the database and receive a Cursor back
        Cursor cursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        // Move the cursor to a valid database row
        if(cursor.moveToFirst()){
            int city = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
            String city_name = cursor.getString(city);
            System.out.println("[rfsaidel] city_name: "+city_name);

            double lat = cursor.getDouble(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT));
            System.out.println("[rfsaidel] lat: "+lat);

            double lon = cursor.getDouble(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG));
            System.out.println("[rfsaidel] lon: "+lon);

            String setting = cursor.getString(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING));
            System.out.println("[rfsaidel] setting: "+setting);
        }
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        validateCurrentRecord("FAIL",cursor,location);

        // Finally, close the cursor and database
        db.close();
        cursor.close();
        return row;
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        long rowLocation = testLocationTable();

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues contentValuesWeather = new ContentValues();
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY,rowLocation);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_DATE,"2017");
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_DEGREES,10);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY,85);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,25.6);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,12.5);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE,0.98);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,"muito calor");
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,1);
        contentValuesWeather.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,10.5);


        // Insert ContentValues into database and get a row ID back
        long rowWeather = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,contentValuesWeather);
        System.out.println("[rfsaidel] rowWeather:"+rowWeather);
        // Query the database and receive a Cursor back

        Cursor cursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME,
                null,null,null,null,null,null,null);
        // Move the cursor to a valid database row
        assertTrue(cursor.moveToFirst());
        System.out.println("[rfsaidel] descWeather:"+cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC)));


        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        validateCurrentRecord("FAIL",cursor,contentValuesWeather);
        // Finally, close the cursor and database
        db.close();
        cursor.close();
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation() {
        return -1L;
    }


    public void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
