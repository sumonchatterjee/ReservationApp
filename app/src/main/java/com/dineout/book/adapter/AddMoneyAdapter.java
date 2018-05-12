package com.dineout.book.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.view.contract.AddMoneyContract;
import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONObject;


public class AddMoneyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private JSONObject mData;
    private final int HEADER = 0x01;
    private final int AMOUNT = 0x02;
    private final int FOOTER = 0x03;

    private AddMoneyContract.AmountContract amountContract;
    private AddMoneyContract.FooterContract footerContract;
    public AddMoneyAdapter(Context context, JSONObject data,AddMoneyContract.AmountContract amountContract ,
                           AddMoneyContract.FooterContract footerContract){

        this.mContext = context;
        this.mData = data;
        this.amountContract = amountContract;
        this.footerContract = footerContract;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

       switch (viewType){

           case HEADER:{

               return new HeaderHolder(LayoutInflater.from(mContext).inflate(R.layout.add_money_header_item,parent,false));
           }
           case AMOUNT:{

               return new AmountHolder(LayoutInflater.from(mContext).inflate(R.layout.add_money_amount_item,parent,false),
                       amountContract);
           }
           case FOOTER:{
               return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.add_money_footer_item,parent,false),
                       footerContract);
           }
           default:
               return null;
       }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder != null) {
                String key = mData.optJSONArray("section").optJSONObject(position).optString("section_key");
                JSONObject section = mData.optJSONObject("section_data").optJSONObject(key).optJSONObject("data");
                if (holder instanceof HeaderHolder) {

                    int paymentType = mData.optInt("payment_type");
                    int id = paymentType == PaymentConstant.FREECHARGE ? R.drawable.img_fc_title :
                            (paymentType == PaymentConstant.MOBIKWIK ? R.drawable.img_mobikwik_title : R.drawable.img_paytm_title);
                    ((HeaderHolder) holder).setTotalAmount(section.optString("title1"));
                    ((HeaderHolder) holder).setCurrentBalance(section.optString("title2"));
                    ((HeaderHolder) holder).setWalletImage(id);

                } else if (holder instanceof AmountHolder) {

                    ((AmountHolder) holder).mAmount.addTextChangedListener(((AmountHolder) holder));
                    ((AmountHolder) holder).setAmount(section.optDouble("amount"));
                    ((AmountHolder) holder).setMinimumField(section.optString("title1"));
                } else if (holder instanceof FooterHolder) {
                    ((FooterHolder) holder).setLabel(section.optString("title1"));
                }
            }
        } catch (Exception e) {
            // exception
            if (holder instanceof HeaderHolder) {
                ((HeaderHolder) holder).itemView.setVisibility(View.GONE);

            } else if (holder instanceof AmountHolder) {
                ((AmountHolder) holder).itemView.setVisibility(View.GONE);

            } else if (holder instanceof FooterHolder) {
                ((FooterHolder) holder).itemView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
       return mData.optJSONArray("section").length();
    }

    @Override
    public int getItemViewType(int position) {
        String key =
                mData.optJSONArray("section").optJSONObject(position).optString("section_type");
        if(key.equalsIgnoreCase("header")){
         return HEADER;
        }else if(key.equalsIgnoreCase("edit_amount")){
            return AMOUNT;
        }else if(key.equalsIgnoreCase("button")){
            return FOOTER;
        }


        return -1;
    }

    class HeaderHolder extends RecyclerView.ViewHolder{


        private TextView mTotalAmount;
        private TextView mCurrentBal;
        private ImageView mWalletImage;
        public HeaderHolder(View itemView) {
            super(itemView);
            mTotalAmount = (TextView)itemView.findViewById(R.id.add_money_total);
            mCurrentBal = (TextView)itemView.findViewById(R.id.add_money_current);
            mWalletImage = (ImageView)itemView.findViewById(R.id.wallet_image);
        }

        public void setTotalAmount(String amount){

            mTotalAmount.setText(AppUtil.renderRupeeSymbol(amount));
        }

        public void setCurrentBalance(String balance){

            mCurrentBal.setText(AppUtil.renderRupeeSymbol(balance));
        }

        public void setWalletImage(int image){

            mWalletImage.setImageResource(image);
        }
    }

    class AmountHolder extends RecyclerView.ViewHolder implements TextWatcher {

        private AddMoneyContract.AmountContract mAmountContract;
        private EditText mAmount;
        private TextView mMinAmount;
        public AmountHolder(View itemView, AddMoneyContract.AmountContract contract) {
            super(itemView);
            this.mAmountContract = contract;
            mAmount = (EditText)itemView.findViewById(R.id.amount_add_money);
            mMinAmount = (TextView)itemView.findViewById(R.id.minimum_amount_add_money);
        }

        public void setAmount(double amt){

            mAmount.setText(amt+"");
        }


        public void setMinimumField(String label){

            mMinAmount.setText(AppUtil.renderRupeeSymbol(label));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!TextUtils.isEmpty(s)) {
                mAmountContract.updateAmount(Double.parseDouble(s.toString()));
            } else {
                mAmountContract.updateAmount(0L);
            }
        }
    }

    class FooterHolder extends RecyclerView.ViewHolder{

        private Button mConfirm;
        public FooterHolder(View itemView, final AddMoneyContract.FooterContract contract) {
            super(itemView);
            mConfirm = (Button)itemView.findViewById(R.id.btn_add_money);
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contract.confirmAdd();
                }
            });

        }


        public void setLabel(String label){

            mConfirm.setText(label);
        }


    }
}
