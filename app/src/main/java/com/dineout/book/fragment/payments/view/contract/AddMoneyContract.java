package com.dineout.book.fragment.payments.view.contract;

/**
 * Created by prateek.aggarwal on 10/17/16.
 */
public interface AddMoneyContract {



    void navigateToWebView(String link,String post);

    void showProgress();
    void hideProgress();

    void showError(String message);

    interface AmountContract{

        void updateAmount(double amount);


    }

    interface FooterContract{

        void confirmAdd();
    }
}
