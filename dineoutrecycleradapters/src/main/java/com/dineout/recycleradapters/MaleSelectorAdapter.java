package com.dineout.recycleradapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MaleSelectorAdapter extends RecyclerView.Adapter<MaleSelectorAdapter.MaleFemaleViewHolder> {

    private String[] guestCountLst;
    private int maleSelectedPosition;
    private int femaleSelectedPosition;
    private boolean isMaleSelected = false;
    private boolean isFemaleSelected = false;
    private OnItemClickListener mClickListener;
    private String type;

    public MaleSelectorAdapter(String[] planetList, String type) {
        this.guestCountLst = planetList;
        this.type = type;
    }

    public void setMaleSelection(int maleCount) {
        if (maleCount > 0) {
            maleSelectedPosition = maleCount - 1;
            isMaleSelected = true;
            notifyDataSetChanged();
        }
    }

    public void setFemaleSelection(int femaleCount) {
        if (femaleCount > 0) {
            femaleSelectedPosition = femaleCount - 1;
            isFemaleSelected = true;
            notifyDataSetChanged();
        }
    }

    @Override
    public MaleSelectorAdapter.MaleFemaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.male_female_row, parent, false);
        return new MaleFemaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MaleSelectorAdapter.MaleFemaleViewHolder holder, int position) {
        final int itemPosition = position;

        String value = guestCountLst[position];
        holder.text.setText(value);
        holder.text.setTag(value);

        if (type.equalsIgnoreCase("male")) {
            renderMaleContent(holder, position);
        } else {
            renderFemaleContent(holder, position);
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equalsIgnoreCase("male")) {
                    onMaleClick(itemPosition, holder);
                } else {
                    onFemaleClick(itemPosition, holder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return guestCountLst.length;
    }

    private void renderMaleContent(MaleSelectorAdapter.MaleFemaleViewHolder holder, int position) {
        if (position == guestCountLst.length - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (maleSelectedPosition == position && isMaleSelected) {
            holder.itemView.setBackgroundResource(R.drawable.male_female_pressed);
            holder.text.setTextColor(Color.parseColor("#ffffff"));

        } else {
            holder.itemView.setBackgroundResource(R.drawable.male_female_normal);
            holder.text.setTextColor(Color.parseColor("#000000"));
        }
    }

    private void renderFemaleContent(MaleSelectorAdapter.MaleFemaleViewHolder holder, int position) {
        if (position == guestCountLst.length - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (femaleSelectedPosition == position && isFemaleSelected) {
            holder.itemView.setBackgroundResource(R.drawable.male_female_pressed);
            holder.text.setTextColor(Color.parseColor("#ffffff"));

        } else {
            holder.itemView.setBackgroundResource(R.drawable.male_female_normal);
            holder.text.setTextColor(Color.parseColor("#000000"));
        }
    }

    private void onMaleClick(int position, MaleSelectorAdapter.MaleFemaleViewHolder holder) {
        String val = "";

        if (maleSelectedPosition == position && isMaleSelected) {
            notifyItemChanged(maleSelectedPosition);
            isMaleSelected = false;
            maleSelectedPosition = -1;
        } else {
            notifyItemChanged(maleSelectedPosition);
            maleSelectedPosition = position;
            notifyItemChanged(maleSelectedPosition);
            isMaleSelected = true;

        }

        if (maleSelectedPosition > -1) {
            val = (String) holder.text.getTag();
        } else {
            val = "";
        }

        getItemClickListener().onMaleItemClick(val);
    }

    private void onFemaleClick(int position, MaleSelectorAdapter.MaleFemaleViewHolder holder) {
        String value = "";

        if (femaleSelectedPosition == position && isFemaleSelected) {
            notifyItemChanged(femaleSelectedPosition);
            isFemaleSelected = false;
            femaleSelectedPosition = -1;
        } else {
            notifyItemChanged(femaleSelectedPosition);
            femaleSelectedPosition = position;
            notifyItemChanged(femaleSelectedPosition);
            isFemaleSelected = true;
        }

        if (femaleSelectedPosition > -1) {
            value = (String) holder.text.getTag();
        } else {
            value = "";
        }

        getItemClickListener().onFemaleItemClick(value);
    }

    private OnItemClickListener getItemClickListener() {
        return mClickListener;
    }

    public void setItemClickListener(OnItemClickListener upcomingBookingCardClickListener) {
        this.mClickListener = upcomingBookingCardClickListener;
    }

    public interface OnItemClickListener {
        void onMaleItemClick(String item);

        void onFemaleItemClick(String item);
    }

    class MaleFemaleViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rootView;
        private TextView text;
        private View divider;

        public MaleFemaleViewHolder(final View itemView) {
            super(itemView);

            rootView = (RelativeLayout) itemView.findViewById(R.id.root_view);
            text = (TextView) itemView.findViewById(R.id.text_id);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}