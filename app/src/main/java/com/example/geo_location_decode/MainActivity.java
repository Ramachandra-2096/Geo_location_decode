package com.example.geo_location_decode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new BackgroundTask().execute();
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<Address> addressList;
            Address address = null;
            AssetManager assetManager = MainActivity.this.getAssets();
            Geocoder geo=new Geocoder(MainActivity.this, Locale.getDefault());
            try {
                InputStream inputStream = assetManager.open("HospitalsInIndia.csv");
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                List<String> indianStates = Arrays.asList(
                        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
                        "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
                        "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
                        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal", "Arunachal Pradesh", "Assam",
                        "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Sikkim", "Tripura"
                );
                // Now you can use the inputStream to read the contents of the CSV file
                CSVReader csvReader = new CSVReader(inputStreamReader);
                csvReader.readNext();
                List<String[]> records = csvReader.readAll();
                try {
                    for (String state : indianStates) {
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Hospital").child(state);
                        Iterator<String[]> iterator = records.iterator();

                        while (iterator.hasNext()) {
                            String[] loc = iterator.next();

                            if (state.toLowerCase().contains(loc[2].toLowerCase())) {
                                DatabaseReference db = mDatabase.child(loc[1].replace(".", " "));

                                try {
                                    addressList = geo.getFromLocationName(loc[1] + " " +loc[4]+" "+ loc[5], 1);

                                    if (addressList != null && !addressList.isEmpty()) {
                                        address = addressList.get(0);
                                        System.out.println("Hospital " + loc[1] + " Lat : " + address.getLatitude() + " Long : " + address.getLongitude() + " locale " + address.getLocality());
                                        db.child("City").setValue(loc[3]);
                                        db.child("Lat ").setValue(address.getLatitude());
                                        db.child("Long ").setValue(address.getLongitude());
                                        db.child("Pincode").setValue(loc[5]);
                                        db.child("Address").setValue(address.getAddressLine(0));
                                        iterator.remove(); // Remove the element using the iterator
                                    } else {
                                        // No address found
                                        Toast.makeText(MainActivity.this, "No address found for the given location", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    // Handle the exception as needed
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Geocoding error: Check spelling and try again", Toast.LENGTH_SHORT).show();
                    e.printStackTrace(); // Log the exception for further debugging if needed
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (CsvException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            // This method is called on the main thread after the background task is complete.
            // You can update UI components or perform other tasks that require the main thread here.
            Toast.makeText(MainActivity.this, "Background task completed", Toast.LENGTH_SHORT).show();
        }
    }
}