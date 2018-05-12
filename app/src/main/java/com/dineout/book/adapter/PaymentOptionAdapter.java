package com.dineout.book.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.book.R;

import org.json.JSONArray;
import org.json.JSONObject;


public class PaymentOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int PG_OPTION = 0x01;
    private final int WALLET_OPTION = 0x02;
    private final int PHONEPE_OPTION = 0x03;
    private Context mContext;
    private JSONArray mOptions;
    private View.OnClickListener mListener;

    public PaymentOptionAdapter(Context context, JSONArray options, View.OnClickListener listener) {

        this.mContext = context;
        this.mOptions = options;
        this.mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == WALLET_OPTION) {
            return new WalletOptionHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.payment_wallet_item, parent, false));
        }
        else if( viewType == PHONEPE_OPTION) {

            return new PaymentGatewayOptionHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.payment_item, parent, false));
        }
        else {
            return new PaymentGatewayOptionHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.payment_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        try {
            if (getItemViewType(position) == PG_OPTION) {
                ((PaymentGatewayOptionHolder) holder).getLabel()
                        .setText(mOptions.getJSONObject(position).optString("text").trim().replaceAll("  ", ""));
                ((PaymentGatewayOptionHolder) holder).setTag(mOptions.getJSONObject(position));

                ((PaymentGatewayOptionHolder) holder).setHolderBackground();

//                ((PaymentGatewayOptionHolder)holder).getContainer().setBackgroundResource(R.drawable.rect_white_bcg);
            }
            else if(getItemViewType(position) == PHONEPE_OPTION){

//                ((PaymentGatewayOptionHolder) holder).getLabel()
//                        .setText(mOptions.getJSONObject(position).optString("text").trim().replaceAll("  ", ""));

                ((PaymentGatewayOptionHolder) holder).getLabel()
                        .setText("PhonePe");

                ((PaymentGatewayOptionHolder) holder).getLabel()
                        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.img_phonepe,0,0,0);
                ((PaymentGatewayOptionHolder) holder).setTag(mOptions.getJSONObject(position));

                ((PaymentGatewayOptionHolder) holder).setHolderBackground();


            }
            else {

                ((WalletOptionHolder) holder).setTag(mOptions.getJSONObject(position));
                boolean linked = mOptions.optJSONObject(position).optString("linked", "0").equalsIgnoreCase("1") ? true : false;
                if (mOptions.getJSONObject(position).optDouble("wallet_amt") >= 0 && linked) {
                    ((WalletOptionHolder) holder).getAmount().setText(String.format(
                            mContext.getResources().getString(R.string.wallet_amount)
                            , mOptions.getJSONObject(position).optDouble("wallet_amt")));
                    ((WalletOptionHolder) holder).getAmount().setVisibility(View.VISIBLE);
                } else {
                    ((WalletOptionHolder) holder).getAmount().setVisibility(View.GONE);
                }

                if (mOptions.getJSONObject(position).optString("type").toLowerCase().contains("mobikwik")) {

                    ((WalletOptionHolder) holder).getWallet().
                            setImageResource(R.drawable.img_mobikwik_option);
                } else if (mOptions.getJSONObject(position).optString("type").toLowerCase().contains("paytm")) {

                    ((WalletOptionHolder) holder).getWallet().
                            setImageResource(R.drawable.img_paytm_option);
                }else if(mOptions.getJSONObject(position).optString("type").toLowerCase().contains("freecharge")){
                    ((WalletOptionHolder) holder).getWallet().
                            setImageResource(R.drawable.img_fc_option);

                }

                ((WalletOptionHolder) holder).setHolderBackground();
//                ((WalletOptionHolder)holder).getContainer().setBackgroundResource(R.drawable.rect_white_bcg);

            }
        } catch (Exception e) {

        }
    }

    @Override
    public int getItemCount() {

        if (mOptions != null)
            return mOptions.length();

        return 0;
    }


    @Override
    public int getItemViewType(int position) {


            JSONObject options = mOptions.optJSONObject(position);
            if (options.optString("type").toLowerCase().contains("wallet"))
                return WALLET_OPTION;
            else if(options.optString("type").equalsIgnoreCase("phonepe")){
                return PHONEPE_OPTION;
            }
            else {
                return PG_OPTION;
            }
    }



    private class PaymentGatewayOptionHolder extends RecyclerView.ViewHolder {

        private TextView label;
        private View container;

        public PaymentGatewayOptionHolder(View itemView) {
            super(itemView);
            container = itemView;
            itemView.setOnClickListener(mListener);
            label = (TextView) itemView.findViewById(R.id.payment_option_label);
        }

        public TextView getLabel() {
            return label;
        }

        public void setTag(JSONObject object) {
            container.setTag(object);
        }

        public View getContainer() {
            return container;
        }

        public void setHolderBackground() {
            if (getAdapterPosition() == 0) {
                getContainer().setBackgroundResource(R.drawable.ripple_drawable_top_rounded);
            } else if (getAdapterPosition() == getItemCount() - 1) {
                getContainer().setBackgroundResource(R.drawable.ripple_drawable_bottom_rounded);
            } else {
                getContainer().setBackgroundResource(R.drawable.ripple_drawable_rounded);
            }
        }
    }

    private class WalletOptionHolder extends RecyclerView.ViewHolder {

        private TextView amount;
        private ImageView wallet;
        private View container;

        public WalletOptionHolder(View itemView) {
            super(itemView);
            container = itemView;
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            container.setLayoutParams(params);
            itemView.setOnClickListener(mListener);
            amount = (TextView) itemView.findViewById(R.id.payment_option_wallet_amount);
            wallet = (ImageView) itemView.findViewById(R.id.payment_option_wallet_icon);
        }

        public TextView getAmount() {
            return amount;
        }

        public ImageView getWallet() {
            return wallet;
        }

        public void setTag(JSONObject object) {
            container.setTag(object);
        }

        public View getContainer() {
            return container;
        }

        public void setHolderBackground() {
            if (getAdapterPosition() == 0) {
                getContainer().setBackgroundResource(R.drawable.ripple_drawable_top_rounded);
            } else if (getAdapterPosition() == getItemCount() - 1) {
                getContainer().setBackgroundResource(R.drawable.ripple_drawable_bottom_rounded);
            } else {
                getContainer().setBackgroundResource(R.drawable.ripple_drawable_rounded);
            }
        }
    }
}
