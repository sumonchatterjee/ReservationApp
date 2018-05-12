package com.dineout.book.fragment.payments;

/**
 * Created by prateek.aggarwal on 28/01/16.
 */
public interface PaymentConstant {

    /**
     * Biller name for which payment need to be made param
     */
    String RESTAURANT_NAME = "payment.NAME";
    /**
     * Biller ID param
     */
    String RESTAURANT_ID = "payment.RESTID";
    /**
     * Booking for which payment need to be made param
     */
    String BOOKING_ID = "payment.BOOKINGID";
    /**
     * Payment amount for corresponding booking param
     */
    String BOOKING_AMT = "payment.AMT";
    /**
     * Whether wallet used param
     */
    String IS_WALLET_ACCEPTED = "payment.WALLET";

    /**
     * DO WALLET AMT
     **/
    String DO_WALLET_AMOUNT = "payment.DOWALLET";
    /**
     * Payment display booking ID
     */
    String DISPLAY_ID = "payment.DISPLAYID";

    String WALLET_TYPE = "payment.WALLETTYPE";

    String WALLET_LINK_NUMBER = "payment.LINKNUMBER";

    String DO_HASH = "payment.DOHASH";

    String PAYMENT_STATUS = "payment.status";

    String FINAL_AMOUNT = "payment.FINAL";

    String PAYMENT_TYPE = "payment.TYPE";

    /**
     * Booking for which payment need to be made param
     */


    String DO_RESTAURANT_NAME = "payment.restaurant.DOWALLET";


    String RESTAURANT_AMT = "payment.restaurant.AMT";


    int PAYTM = 1;
    int MOBIKWIK = 2;
    int FREECHARGE = 4;
    int PHONEPE = 5;


}
