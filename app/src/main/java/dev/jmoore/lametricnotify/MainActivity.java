package dev.jmoore.lametricnotify;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Stores what apps are enabled as well as the IP and API key
    private SharedPreferences settings;

    // List of apps to pass to initializeView so we don't freeze the device with every search
    private final List<AppItem> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read our saved "settings" (enabled apps, IP, api key)
        settings = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);

        // Get a list of apps
        // This loop runs   s l o w l y   on launch. This is normal.
        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
            appList.add(new AppItem(packageInfo.loadIcon(getPackageManager()), packageInfo.loadLabel(getPackageManager()).toString(), packageInfo.packageName));

        // Sort the apps alphabetically A-Z
        Collections.sort(appList, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem o1, AppItem o2) {
                return o1.getAppName().compareToIgnoreCase(o2.getAppName());
            }
        });

        // Set up the recycler view
        initializeView("", true);

        // Set the address/api values to what's stored in settings
        EditText txtAddress = (EditText) findViewById(R.id.textAddress);
        EditText txtApi = (EditText) findViewById(R.id.textApi);
        txtAddress.setText(settings.getString("address", "IP Address")); // TODO: Set defValue using @string resource
        txtApi.setText(settings.getString("api", "Device API Key")); // TODO: Set defValue using @string resource

        // Add a click listener to the save button
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText txtAddress = (EditText) findViewById(R.id.textAddress);
                EditText txtApi = (EditText) findViewById(R.id.textApi);

                // Save the settings
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("address", txtAddress.getText().toString());
                editor.putString("api", txtApi.getText().toString());
                editor.apply();

                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        // Add a click listener to the help button
        Button helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelp();
            }
        });

        // Add a listener to the search box
        EditText txtSearch = (EditText) findViewById(R.id.textSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                initializeView(s.toString(), false);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initializeView(String search, boolean firstRun) {
        // Set up the RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.appList);

        if (firstRun) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        }

        recyclerView.setAdapter(new AppAdapter(appList, search));
    }

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Help");
        builder.setMessage("1. The app will only work when connected to the same WiFi network as your LaMetric Time.\n\n" +
                "2. Enter the IP address of your LaMetric Time. This can be found in the LaMetric app: select your device > gear icon > Connectivity\n\n" +
                "3. The API key is the LOCAL API key for your device. This can be obtained at (sign in with your developer account): https://developer.lametric.com/user/devices\n\n" +
                "4. Enable apps that you want to display notifications for. Certain apps (Instagram, Facebook, etc.) have \"fancy\" icons where others have a generic icon.\n\n" +
                "5. Make sure you allow Notification Access in Settings > Apps > Special Access > Notification Access\n\n" +
                "\n" +
                "Have a suggestion or question? Feel free to visit the GitHub page and open an Issue or Pull Request!");

        builder.setNeutralButton("GitHub", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tycrek/lametric-notify-plus"));
                startActivity(browserIntent);
            }
        });

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        // Display Help dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}