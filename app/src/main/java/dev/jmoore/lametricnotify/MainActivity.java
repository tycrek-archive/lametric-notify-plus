package dev.jmoore.lametricnotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public static final String DIALOG_TITLE_PERMISSION_REQUIRED = "Permission required";
    public static final String DIALOG_TITLE_CONFIRM_NETWORK_NAME = "Confirm network name";
    public static final String DIALOG_TITLE_ENTER_NETWORK_NAME = "Enter network name";
    public static final String DIALOG_TITLE_HELP = "Help";

    public static final String DIALOG_MESSAGE_NOTIFICATIONS = "The app needs permission to view your notifications in order to send them to your LaMetric Time.";
    public static final String DIALOG_MESSAGE_LOCATION = "The app needs location permission to get your WiFi network name. This is used to make sure notifications are only sent when you are on the same network as your LaMetric device.\n\nIf you do not want to provide Location, you may also manually set the WiFi SSID.";
    public static final String DIALOG_MESSAGE_CONFIRM_NETWORK_NAME = "Please confirm that %s is your network name.";
    public static final String DIALOG_MESSAGE_HELP = "1. The app will only work when connected to the same WiFi network as your LaMetric Time.\n\n" +
            "2. Enter the IP address of your LaMetric Time. This can be found in the LaMetric app: select your device > gear icon > Connectivity\n\n" +
            "3. The API key is the LOCAL API key for your device. This can be obtained at (sign in with your developer account): https://developer.lametric.com/user/devices\n\n" +
            "4. Enable apps that you want to display notifications for. Certain apps (Instagram, Facebook, etc.) have \"fancy\" icons where others have a generic icon.\n\n" +
            "5. Make sure you allow Notification Access in Settings > Apps > Special Access > Notification Access\n\n\n" +
            "Have a suggestion or question? Feel free to visit the GitHub page and open an Issue or Pull Request!";

    public static final String DIALOG_BUTTON_CONTINUE = "Continue";
    public static final String DIALOG_BUTTON_CONFIRM = "Confirm";
    public static final String DIALOG_BUTTON_MODIFY = "Modify";
    public static final String DIALOG_BUTTON_GITHUB = "GitHub";
    public static final String DIALOG_BUTTON_CLOSE = "Close";

    public static final String SHARED_PREFERENCES_NAME = "settings";
    public static final String SHARED_PREFERENCES_KEY_ADDRESS = "address";
    public static final String SHARED_PREFERENCES_KEY_API = "api";
    public static final String SHARED_PREFERENCES_KEY_SSID = "ssid";

    public static final String SHARED_PREFERENCES_DEFAULT_API = "api_key";
    public static final String SHARED_PREFERENCES_DEFAULT_ADDRESS = "0.0.0.0";

    public static final String TOAST_SAVED = "Saved";

    public static final String NOTIFICATION_EXTRA_TITLE = "android.title";
    public static final String NOTIFICATION_EXTRA_TEXT = "android.text";

    public static final String LAMETRIC_FRAMES = "{ \"icon\": %s, \"text\": \"%s\" }";
    public static final String LAMETRIC_MODEL = "{ \"model\": { \"frames\": [ %s ] } }";
    public static final String LAMETRIC_URL = "http://%s:8080/api/v2/device/notifications";

    public static final String HEADER_KEY_AUTH = "Authorization";
    public static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_KEY_ACCEPT = "Accept";

    public static final String HEADER_AUTH = "Basic %s";
    public static final String HEADER_JSON = "application/json";

    public static final String STRING_EMPTY = "";
    public static final String STRING_GITHUB_REPO = "https://github.com/tycrek/lametric-notify-plus";

    // Stores what apps are enabled as well as the IP and API key
    private SharedPreferences settings;

    // List of apps to pass to initializeView so we don't freeze the device with every search
    private List<AppItem> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make sure we have permissions
        checkNotificationPermissions();
        checkLocationPermission();

        // Read our saved "settings" (enabled apps, IP, api key)
        settings = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        // Get a list of apps
        appList = getAppList();

        // Set up the UI
        initializeView("", true);
    }

    /**
     * Checks if we have notification permission and requests it if not
     */
    private void checkNotificationPermissions() {
        // Check if we have Notification Access enabled in Special Access
        if (!NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()).contains(getApplicationContext().getPackageName())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(DIALOG_TITLE_PERMISSION_REQUIRED);
            builder.setMessage(DIALOG_MESSAGE_NOTIFICATIONS);

            builder.setPositiveButton(DIALOG_BUTTON_CONTINUE, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // This opens Notification Access under Special Access in Android Settings
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * Checks if we have location permission. Also checks SSID
     */
    private void checkLocationPermission() {
        // Check if we are missing any of the location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // This dialog is different from the others because it causes problems if not written like this.
            // By this I mean not setting any objects and just calling new
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(DIALOG_TITLE_PERMISSION_REQUIRED)
                    .setMessage(DIALOG_MESSAGE_LOCATION)
                    .setPositiveButton(DIALOG_BUTTON_CONTINUE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestLocationPermission();
                        }
                    })
                    .create().show();
        }
    }

    /**
     * Initialize the recycler view
     *
     * @param search   Search term to filter recycler view
     * @param firstRun Some initialization on the recycler view can only be done once
     */
    private void initializeView(String search, boolean firstRun) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.appList);

        // Called when the app is opened to set up the UI
        if (firstRun) {
            final EditText txtAddress = (EditText) findViewById(R.id.textAddress);
            final EditText txtApi = (EditText) findViewById(R.id.textApi);
            final EditText txtSearch = (EditText) findViewById(R.id.textSearch);
            final Button saveButton = (Button) findViewById(R.id.saveButton);
            final Button helpButton = (Button) findViewById(R.id.helpButton);

            // Set up the RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

            // Set the address/api values to what's stored in settings
            txtAddress.setText(settings.getString(SHARED_PREFERENCES_KEY_ADDRESS, STRING_EMPTY)); // TODO: Set defValue using @string resource
            txtApi.setText(settings.getString(SHARED_PREFERENCES_KEY_API, STRING_EMPTY)); // TODO: Set defValue using @string resource

            // Add a click listener to the save button
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Save the settings
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(SHARED_PREFERENCES_KEY_ADDRESS, txtAddress.getText().toString());
                    editor.putString(SHARED_PREFERENCES_KEY_API, txtApi.getText().toString());
                    editor.apply();

                    // Alert the user that settings are saved
                    Toast.makeText(getApplicationContext(), TOAST_SAVED, Toast.LENGTH_SHORT).show();
                }
            });

            // Add a click listener to the help button
            helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHelp();
                }
            });

            // Add a listener to the search box
            txtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    initializeView(s.toString(), false);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        recyclerView.setAdapter(new AppAdapter(appList, search));
    }

    /**
     * Requests location permissions from the user
     */
    private void requestLocationPermission() {
        // Starting with Android 10 (Android Q) we need to explicitly request ACCESS_BACKGROUND_LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    /**
     * Asks the user if the current wifi SSID is the correct one
     *
     * @param override Used if getSsidFromUser() was required
     * @param mSsid    SSID to use when override = true
     */
    private void confirmSsid(boolean override, String mSsid) {
        final String ssid = override ? mSsid : getSsid();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(DIALOG_TITLE_CONFIRM_NETWORK_NAME);
        builder.setMessage(String.format(DIALOG_MESSAGE_CONFIRM_NETWORK_NAME, ssid));

        builder.setPositiveButton(DIALOG_BUTTON_CONFIRM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save the SSID in SharedPreferences
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(SHARED_PREFERENCES_KEY_SSID, ssid);
                editor.apply();
            }
        });

        // If the user taps modify then we need to ask them what their network name is
        builder.setNegativeButton(DIALOG_BUTTON_MODIFY, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getSsidFromUser();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Open a dialog box to request a custom SSID from the user
     */
    private void getSsidFromUser() {
        // This EditText is only needed to get the custom SSID
        final EditText input = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(DIALOG_TITLE_ENTER_NETWORK_NAME);
        builder.setView(input);

        builder.setPositiveButton(DIALOG_BUTTON_CONTINUE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Confirm again just in case the user made a mistake
                confirmSsid(true, input.getText().toString());
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Shows basic tips and general troubleshooting
     */
    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(DIALOG_TITLE_HELP);
        builder.setMessage(DIALOG_MESSAGE_HELP);

        builder.setNeutralButton(DIALOG_BUTTON_GITHUB, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(STRING_GITHUB_REPO));
                startActivity(browserIntent);
            }
        });

        builder.setPositiveButton(DIALOG_BUTTON_CLOSE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Display Help dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Get a list of apps installed on the device
     *
     * @return List of apps installed on the device
     */
    private List<AppItem> getAppList() {
        List<AppItem> tmpList = new ArrayList<>();

        // This loop runs   s l o w l y   on launch. This is normal.
        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
            tmpList.add(new AppItem(packageInfo.loadIcon(getPackageManager()), packageInfo.loadLabel(getPackageManager()).toString(), packageInfo.packageName));

        // Sort the apps alphabetically A-Z
        Collections.sort(tmpList, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem o1, AppItem o2) {
                return o1.getAppName().compareToIgnoreCase(o2.getAppName());
            }
        });

        return tmpList;
    }

    /**
     * Gets the SSID for the active wifi network
     *
     * @return SSID for the active wifi network
     */
    private String getSsid() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();

        return wifiInfo.getSupplicantState() == SupplicantState.COMPLETED ? wifiInfo.getSSID() : STRING_EMPTY;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Confirm the SSID or ask for a custom SSID
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                confirmSsid(false, STRING_EMPTY);
            } else {
                getSsidFromUser();
            }
        }
    }
}
