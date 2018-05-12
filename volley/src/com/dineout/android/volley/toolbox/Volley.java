/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dineout.android.volley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.text.TextUtils;

import com.dineout.android.volley.Network;
import com.dineout.android.volley.RequestQueue;
import com.dineout.dineoutssl.DineoutSSLPinning;

import java.io.File;

import javax.net.ssl.SSLContext;

public class Volley {

	/** Default on-disk cache directory. */
	public static final String DEFAULT_CACHE_DIR = "volley";
	public static final String DEFAULT_IMAGE_CACHE_DIR = "volleyImages";

	/**
	 * Creates a default instance of the worker pool and calls
	 * {@link RequestQueue#start()} on it.
	 *
	 * @param context
	 *            A {@link Context} to use for creating the cache dir.
	 * @param stack
	 *            An {@link HttpStack} to use for the network, or null for
	 *            default.
	 * @return A started {@link RequestQueue} instance.
	 */
	public static RequestQueue newRequestQueue(Context context,
			HttpStack stack, String cacheDirName, int threadPool,
			boolean enableCompression, boolean enableThreadingLogic,DineoutSSLPinning sslPinning) {
		if (TextUtils.isEmpty(cacheDirName)) {
			cacheDirName = DEFAULT_CACHE_DIR;
		}
		File cacheDir = new File(context.getCacheDir(), cacheDirName);

		SSLContext sslContext= sslPinning !=null ? sslPinning.getSslContext() : null;
		String userAgent = "volley/0";
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(
					packageName, 0);
			userAgent = packageName + "/" + info.versionCode;
		} catch (NameNotFoundException e) {
		}

		if (stack == null) {
			//if (Build.VERSION.SDK_INT >= 9) {
			if(sslContext!=null)
				stack = new HurlStack(null,sslContext.getSocketFactory());
			else {
				stack= new HurlStack();
			}
//			} else {
//				// Prior to Gingerbread, HttpUrlConnection was unreliable.
//				// See:
//				// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
//				stack = new HttpClientStack(
//						AndroidHttpClient.newInstance(userAgent));
//			}
		}

		Network network = new BasicNetwork(stack);

		RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir,
				enableCompression), network, threadPool, enableThreadingLogic);

		return queue;
	}

	/**
	 * Creates a default instance of the worker pool and calls
	 * {@link RequestQueue#start()} on it.
	 *
	 * @param context
	 *            A {@link Context} to use for creating the cache dir.
	 * @return A started {@link RequestQueue} instance.
	 */
//	public static RequestQueue newRequestQueue(Context context, int threadPool,
//			boolean enableCompression, boolean enableTheadingLogic) {
//		return newRequestQueue(context, null, null, threadPool,
//				enableCompression, enableTheadingLogic);
//	}
//
//	public static RequestQueue newRequestQueue(Context context,
//			String cacheDirName, int threadPool, boolean enableCompression,
//			boolean enableTheadingLogic) {
//		return newRequestQueue(context, null, cacheDirName, threadPool,
//				enableCompression, enableTheadingLogic);
//	}
}
