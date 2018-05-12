package com.dineout.book.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.book.R;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;

import java.util.ArrayList;
import java.util.List;


public class StoredCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<StoredCard> mCards;
    private int expandedPosition = 0;
    private CVVValidListener mListener;
    private View.OnClickListener mDeleteListener;
    public StoredCardAdapter(Context context, ArrayList<StoredCard> cards){

        this.mContext = context;
        this.mCards = cards;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StoredCardHolder(LayoutInflater.from(mContext).inflate(R.layout.saved_card_item,parent,false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder != null && mCards.get(position) != null){

            ((StoredCardHolder)holder).initializeSaveCard(mCards.get(position));
            ((StoredCardHolder)holder).updateBackground(position%2 == 0 ? R.drawable.saved_card_even_bcg:R.drawable.saved_card_odd_bcg);

            ((StoredCardHolder)holder).showExpandedView(position == expandedPosition);
        }
    }

    public void setDeleteListener(View.OnClickListener listener){
        this.mDeleteListener = listener;
    }

    public void setCVVValidateListener(CVVValidListener listener){

        this.mListener = listener;
    }

    @Override
    public int getItemCount() {

        if(mCards == null)
            return 0;
        else
           return mCards.size();
    }

    private class StoredCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private View mContainerView;
        private TextView mCollapsedNumber;
        private TextView mExpandedNumber;
        private ImageView mCollapsedIssuer;
        private ImageView mExpandedIssuer;
        private TextView mCardName;
        private EditText mCvvView;
        private View mDeleteView;
        private View mCollapsedContainer;
        private View mExpandedContainer;
        public StoredCardHolder(View itemView) {
            super(itemView);
            mContainerView = itemView;
            mCollapsedNumber= (TextView)mContainerView.findViewById(R.id.saved_card_number);
            mExpandedNumber = (TextView)mContainerView.findViewById(R.id.saved_card_expanded_number);
            mCollapsedIssuer = (ImageView)mContainerView.findViewById(R.id.saved_card_issuer);
            mExpandedIssuer = (ImageView)mContainerView.findViewById(R.id.saved_card_expanded_issuer);
            mCardName = (TextView)mContainerView.findViewById(R.id.saved_card_user_name);
            mCvvView  = (EditText)mContainerView.findViewById(R.id.saved_card_cvv);
            mDeleteView = mContainerView.findViewById(R.id.delete_save_card);
            mCollapsedContainer = mContainerView.findViewById(R.id.saved_card_collapsed_container);
            mExpandedContainer = mContainerView.findViewById(R.id.saved_card_expanded_container);
            mExpandedContainer.setOnClickListener(this);
            mCollapsedContainer.setOnClickListener(this);
        }

        public void initializeSaveCard(StoredCard card){

            if(card != null){
                if(!TextUtils.isEmpty(card.getMaskedCardNumber())){
                    mCollapsedNumber.setText(card.getMaskedCardNumber());
                    mExpandedNumber.setText(card.getMaskedCardNumber());
                    mCollapsedIssuer.setImageDrawable(getIssuerDrawable(new PayuUtils().getIssuer(card.getCardBin())));
                    mExpandedIssuer.setImageDrawable(getIssuerDrawable(new PayuUtils().getIssuer(card.getCardBin())));
                    mCardName.setText(card.getNameOnCard());
                    mDeleteView.setTag(card);
                    mDeleteView.setOnClickListener(mDeleteListener);
                    mCvvView.setTag(card);
                    mCvvView.addTextChangedListener(new CvvWatcher(mCvvView,
                            card));

                }
            }

        }

        public void updateBackground(int res){

            mCollapsedContainer.setBackgroundResource(res);
            mExpandedContainer.setBackgroundResource(res);
        }

        private Drawable getIssuerDrawable(String issuer){

            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                switch (issuer) {
                    case PayuConstants.VISA:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.visa);
                    case PayuConstants.LASER:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.laser);
                    case PayuConstants.DISCOVER:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.discover);
                    case PayuConstants.MAES:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.maestro);
                    case PayuConstants.MAST:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.master);
                    case PayuConstants.AMEX:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.amex);
                    case PayuConstants.DINR:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.diner);
                    case PayuConstants.JCB:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.jcb);
                    case PayuConstants.SMAE:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.maestro);
                    case PayuConstants.RUPAY:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.rupay);
                }
                return null;
            }else {

                switch (issuer) {
                    case PayuConstants.VISA:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.visa, null);
                    case PayuConstants.LASER:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.laser, null);
                    case PayuConstants.DISCOVER:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.discover, null);
                    case PayuConstants.MAES:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.maestro, null);
                    case PayuConstants.MAST:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.master, null);
                    case PayuConstants.AMEX:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.amex, null);
                    case PayuConstants.DINR:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.diner, null);
                    case PayuConstants.JCB:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.jcb, null);
                    case PayuConstants.SMAE:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.maestro, null);
                    case PayuConstants.RUPAY:
                        return mContext.getResources().getDrawable(com.payu.payuui.R.drawable.rupay, null);
                }
                return null;
            }
        }

        @Override
        public void onClick(View v) {

            if(v == mCollapsedContainer){
                notifyItemChanged(expandedPosition);
                expandedPosition = this.getPosition();
               showExpandedView(true);
                mListener.updateStoredCard(null, null);


            }
            else if(v == mExpandedContainer){
                showExpandedView(false);
                mListener.updateStoredCard(null,null);

            }else if(v == mDeleteView){

            }

        }

        public void showExpandedView(boolean expanded){
            if(expanded){
                mExpandedContainer.setVisibility(View.VISIBLE);
                mCollapsedContainer.setVisibility(View.GONE);

            }else{
                mCollapsedContainer.setVisibility(View.VISIBLE);
                mExpandedContainer.setVisibility(View.GONE);
            }

        }
    }

    public interface CVVValidListener{

        void updateStoredCard(StoredCard card, String cvv);
    }

   private class CvvWatcher implements TextWatcher{

       private EditText mCVVView;
       private StoredCard card;
       private PayuUtils payuUtils;

       public CvvWatcher(EditText view,StoredCard card){
           this.mCVVView = view;
           this.card = card;
           payuUtils = new PayuUtils();
       }
       @Override
       public void beforeTextChanged(CharSequence s, int start, int count, int after) {

       }

       @Override
       public void onTextChanged(CharSequence s, int start, int before, int count) {

           mCVVView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(payuUtils.getIssuer(card.getCardBin()).contentEquals(PayuConstants.AMEX) ? 4 : 3)});
           if (payuUtils.validateCvv(this.card.getCardBin(), s.toString())) {
               mListener.updateStoredCard(this.card,mCVVView.getText().toString().trim());
           } else {
               mListener.updateStoredCard(null,null);
           }
       }

       @Override
       public void afterTextChanged(Editable s) {

       }
   }



}
