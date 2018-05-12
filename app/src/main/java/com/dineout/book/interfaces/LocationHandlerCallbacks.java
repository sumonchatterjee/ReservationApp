package com.dineout.book.interfaces;


public interface LocationHandlerCallbacks {

    void locationUpdateFailed(String cause);

    void locationUpdateSuccess();

}
