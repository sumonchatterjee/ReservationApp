package com.example.dineoutnetworkmodule;

/**
 * Created by prateek.aggarwal on 7/11/16.
 */
public interface ProgressListener {

    /**
     * Callback method thats called on each byte transfer.
     */
    void onProgress(long transferredBytes, long totalSize);
}
