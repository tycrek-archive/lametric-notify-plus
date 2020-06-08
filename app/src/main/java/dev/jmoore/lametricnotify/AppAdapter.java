package dev.jmoore.lametricnotify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private List<AppItem> appList = new ArrayList<AppItem>();
    private SharedPreferences settings;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView appName;
        public Switch appToggle;

        public ViewHolder(View view) {
            super(view);
            appName = (TextView) view.findViewById(R.id.appName);
            appToggle = (Switch) view.findViewById(R.id.appToggle);
        }
    }

    public AppAdapter(List<ApplicationInfo> packages) {
        for (ApplicationInfo packageInfo : packages) {
            String mName = packageInfo.name;
            String mPackage = packageInfo.packageName;
            this.appList.add(new AppItem(mName, mPackage));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item_row, parent, false);
        this.settings = parent.getContext().getSharedPreferences("settings", MODE_PRIVATE);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final AppItem appItem = appList.get(position);
        holder.appName.setText(appItem.getAppPackage());
        holder.appToggle.setChecked(settings.getBoolean(appItem.getAppPackage(), false));
        holder.appToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(appItem.getAppPackage(), buttonView.isChecked());
                editor.commit();

                //Toast.makeText(buttonView.getContext(), String.valueOf(settings.getBoolean(appItem.getAppPackage(), false)), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}
