package com.dineout.recycleradapters.viewmodel;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.dineout.recycleradapters.R;

import java.util.ArrayList;

/**
 * Created by prateek.aggarwal on 8/24/16.
 */
public class MenuItemViewModel {

    private ArrayList<String> imageList;
    private MenuThumbnailHandler handler;

    public MenuItemViewModel(View v,String title,ArrayList<String> list,MenuThumbnailHandler handler){

        this.imageList = list;
        this.handler = handler;
        
        initializeView(v);
    }

    private void initializeView(View v) {
        addThumbnail((LinearLayout) v.findViewById(R.id.thumbnail_container),this);
    }


    private ArrayList<String> getImageList() {
        return imageList;
    }

    private MenuThumbnailHandler getHandler() {
        return handler;
    }

    private void addThumbnail(LinearLayout layout , final MenuItemViewModel modelMenu){

        Context context = layout.getContext();
        int itemWidth = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_width);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int count = width/(itemWidth+32);
        int remainingCount = 0;
        final ArrayList<String> list= modelMenu.getImageList();
        if(list.size() > count ){
            remainingCount = list.size() - count;
        }else{
            count = list.size();
        }

        for(int i=0;i<count;i++){

          MenuThumbnailViewModel model = null;
            View v = LayoutInflater.from(context).inflate(R.layout.item_menu_thumbnail,null,false);
            if(i == count -1 && remainingCount > 0)
                model = new MenuThumbnailViewModel(v,list.get(i),remainingCount);
            else
                model = new MenuThumbnailViewModel(v,list.get(i));

            final int position = i;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modelMenu.getHandler().showMenuImage(list,position);
                }
            });

            layout.addView(v);

        }
    }
}
