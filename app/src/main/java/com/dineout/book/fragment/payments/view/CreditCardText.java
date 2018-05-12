package com.dineout.book.fragment.payments.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.dineout.book.R;
import com.dineout.book.util.CreditCardUtil;


public class CreditCardText extends CreditEntryFieldBase {
    public static final String CARD_INVALID = "INVALID";
    private String type;

    public CreditCardText(Context context) {
        super(context);
        init();
    }

    public CreditCardText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CreditCardText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressLint("RtlHardcoded")
    @Override
    void init() {
        super.init();
        setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    }

    /* TextWatcher Implementation Methods */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void textChanged(CharSequence s, int start, int before, int end) {
        super.textChanged(s, start, before, end);
        String number = s.toString();
        if (number.length() >= CreditCardUtil.CC_LEN_FOR_TYPE) {
            formatAndSetText(number, start);
        } else {
            if (this.type != null) {
                this.type = null;
                delegate.onCardTypeChange(CARD_INVALID);
            }
        }
    }

    @Override
    public void formatAndSetText(String number, int start) {
        String type = CreditCardUtil.findCardType(number);

        if (type.equals(CARD_INVALID)) {
            setValid(false);
            delegate.onBadInput(this);
            return;
        }

        if (this.type != type) {
            delegate.onCardTypeChange(type);
        }
        this.type = type;

        String formatted = CreditCardUtil.formatForViewing(number, type);
        if (!number.equalsIgnoreCase(formatted)) {
            this.removeTextChangedListener(this);
            this.setText(formatted);
            this.setSelection(formatted.length());
            this.addTextChangedListener(this);
        }

        if (formatted.length() >= CreditCardUtil.lengthOfFormattedStringForType(type)) {

            String remainder = null;
            if (number.startsWith(formatted)) {
                remainder = number.replace(formatted, "");
            }
            if (CreditCardUtil.isValidNumber(formatted)) {
                setValid(true);
                delegate.onCreditCardNumberValid(remainder);
            } else {
                setValid(false);
                delegate.onBadInput(this);
            }
        } else {
            setValid(false);
        }
    }

    public String getType() {
        return type;
    }

    @Override
    public String helperText() {
        return context.getString(R.string.card_hint);
    }
}
