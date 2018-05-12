package com.dineout.book.adapter;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.book.R;

import java.util.List;

public class ShareIntentGridAdapter extends ArrayAdapter<ResolveInfo> {
    Activity context;
    List<ResolveInfo> items;
    boolean[] arrows;
    int layoutId;

    public ShareIntentGridAdapter(Activity context, List<ResolveInfo> items) {
        super(context, R.layout.share_dialog_gridview, items);

        this.context = context;
        this.items = items;
        this.layoutId = R.layout.share_dialog_gridview;
    }

    public View getView(int pos, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View row = inflater.inflate(layoutId, null);

        TextView label = (TextView) row.findViewById(R.id.app_name);
        label.setText(items.get(pos).activityInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
        ImageView image = (ImageView) row.findViewById(R.id.app_logo);
        image.setImageDrawable(items.get(pos).activityInfo.applicationInfo.loadIcon(context.getPackageManager()));
        row.setBackgroundResource(R.drawable.bg_share_clickable);

        return (row);
    }
}
