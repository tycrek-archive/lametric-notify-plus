package dev.jmoore.lametricnotify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        List<ApplicationInfo> packages = getPackages();
        recyclerView = (RecyclerView) findViewById(R.id.appList);

        appAdapter = new AppAdapter(packages);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(appAdapter);

        EditText txtAddress = (EditText) findViewById(R.id.textAddress);
        EditText txtApi = (EditText) findViewById(R.id.textApi);
        txtAddress.setText(settings.getString("address", "IP Address"));
        txtApi.setText(settings.getString("api", "Device API Key"));

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Apple", Toast.LENGTH_SHORT).show();
                EditText txtAddress = (EditText) findViewById(R.id.textAddress);
                EditText txtApi = (EditText) findViewById(R.id.textApi);

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("address", txtAddress.getText().toString());
                editor.putString("api", txtApi.getText().toString());
                editor.commit();

            }
        });
    }

    private List<ApplicationInfo> getPackages() {
        return getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }
}