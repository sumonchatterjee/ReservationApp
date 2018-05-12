package com.dineout.book.interfaces;


public interface UploadStateObserver {



    int TRANSFER_COMPLETED = 0x04;
    int TRANSFER_STARTED = 0x05;
    int TRANSFER_PENDING = 0x06;
    int TRANSFER_ERROR = 0x07;
    int TRANSFER_INTERRUPTED = 0x08;

     void transferStateChanged(int id,int state,Object obj);

     void progressChanged(int id,long completed,long total);
}
