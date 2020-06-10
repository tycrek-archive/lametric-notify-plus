package dev.jmoore.lametricnotify;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private List<AppItem> appList = new ArrayList<>();
    private SharedPreferences settings;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public TextView appPackage;
        public Switch appToggle;

        public ViewHolder(View view) {
            super(view);
            appIcon = (ImageView) view.findViewById(R.id.appIcon);
            appName = (TextView) view.findViewById(R.id.appName);
            appToggle = (Switch) view.findViewById(R.id.appToggle);
            appPackage = (TextView) view.findViewById(R.id.appPackage);
        }
    }

    public AppAdapter(List<AppItem> appItems, String search) {
        if (search == null) search = "";
        else search = search.toLowerCase();

        for (AppItem appItem : appItems) {
            Drawable mIcon = appItem.getAppIcon();
            String mName = appItem.getAppName();
            String mPackage = appItem.getAppPackage();
            if ((mName != null && mName.toLowerCase().contains(search)) || (mPackage != null && mPackage.toLowerCase().contains(search)))
                this.appList.add(appItem);
        }
    }

    @NonNull
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
        holder.appIcon.setImageDrawable(appItem.getAppIcon());
        holder.appName.setText(appItem.getAppName());
        holder.appPackage.setText(appItem.getAppPackage());
        holder.appToggle.setChecked(settings.getBoolean(appItem.getAppPackage(), false));
        holder.appToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(appItem.getAppPackage(), buttonView.isChecked());
                editor.apply();
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.appToggle.setOnCheckedChangeListener(null);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}
