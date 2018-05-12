package com.example.dineoutnetworkmodule;

import android.content.Context;

import com.dineout.android.volley.RequestQueue;
import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.Volley;
import com.dineout.dineoutssl.DineoutSSLPinning;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class ImageRequestManager {

	private static ImageRequestManager instance;

	private ImageLoader imageLoader;
	private RequestQueue queue;
	private static DineoutSSLPinning dineoutSSLPinning;

	private ImageRequestManager(Context context) {
		if (dineoutSSLPinning == null) {
			// Load SSL Certificate
			loadSSLCertificate(context);
		}
		this.queue = Volley.newRequestQueue(context,
				null,
				Volley.DEFAULT_IMAGE_CACHE_DIR, 3, false, true ,dineoutSSLPinning);
		imageLoader = new ImageLoader(queue, new ImageCacheManager());
		this.queue.start();
	}

	public static ImageRequestManager getInstance(Context context) {
		if (instance == null) {
			synchronized (ImageCacheManager.class) {
				if (instance == null) {
					instance = new ImageRequestManager(context);
				}
			}
		}
		return instance;
	}

	private void loadSSLCertificate(Context mContext) {
		try {
			dineoutSSLPinning = DineoutSSLPinning.getInstance();
			dineoutSSLPinning.initiateSSLPinning(mContext);
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}


	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public RequestQueue getQueue() {
		return queue;
	}

}
