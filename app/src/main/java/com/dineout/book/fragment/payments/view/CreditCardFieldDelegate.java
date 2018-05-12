package com.dineout.book.fragment.payments.view;

import android.widget.EditText;

public interface CreditCardFieldDelegate {
    // When the card type is identified
    void onCardTypeChange(String type);

    void onCreditCardNumberValid(String remainder);

    void onBadInput(EditText field);
}
