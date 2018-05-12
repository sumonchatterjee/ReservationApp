/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.dineout.android.volley;

import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;

import com.dineout.android.volley.RequestQueue.GlobalRequestQueueListener;

import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing network dispatch from a queue of requests.
 * 
 * Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 */
@SuppressWarnings("rawtypes")
public class NetworkDispatcher extends Thread {
	
	/** The queue of requests to service. */
	private final BlockingQueue<Request> mQueue;
	private final BlockingQueue<Request> mNormalQueue;
	private final BlockingQueue<Request> mOthersQueue;
	/** The network interface for processing requests. */
	private final Network mNetwork;
	/** The cache to write to. */
	private final Cache mCache;
	/** For posting responses and errors. */
	private final ResponseDelivery mDelivery;
	/** Used for telling us to die. */
	private volatile boolean mQuit = false;
	
	public static final int DEDICATED_DOWNLOAD_MIXED = 0;
	public static final int DEDICATED_DOWNLOAD_NORMAL = 1;
	public static final int DEDICATED_DOWNLOAD_OTHERS = 2;
	
	
	private final int mDedicatedDownload;
	private GlobalRequestQueueListener globalRequestQueueListener;
	
	/**
	 * Creates a new network dispatcher thread. You must call {@link #start()}
	 * in order to begin processing.
	 * 
	 * @param queue
	 *            Queue of incoming requests for triage
	 * @param network
	 *            Network interface to use for performing requests
	 * @param cache
	 *            Cache interface to use for writing responses to cache
	 * @param delivery
	 *            Delivery interface to use for posting responses
	 */
	public NetworkDispatcher(BlockingQueue<Request> queue,BlockingQueue<Request> normalQueue, BlockingQueue<Request> othersQueue, Network network,
			Cache cache, ResponseDelivery delivery,int dedicatedDownload) {
		mQueue = queue;
		mNetwork = network;
		mCache = cache;
		mDelivery = delivery;
		mDedicatedDownload = dedicatedDownload;
		mNormalQueue = normalQueue;
		mOthersQueue = othersQueue;
	}
	
	
	public void setGlobalRequestQueueListener(
			GlobalRequestQueueListener globalRequestQueueListener) {
		this.globalRequestQueueListener = globalRequestQueueListener;
	}
	
	@Override
	public ClassLoader getContextClassLoader() {
		// TODO Auto-generated method stub
		return super.getContextClassLoader();
	}
	
	/**
	 * Forces this dispatcher to quit immediately. If any requests are still in
	 * the queue, they are not guaranteed to be processed.
	 */
	public void quit() {
		mQuit = true;
		interrupt();
	}
	
	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		
		while (true) {
			long startTimeMs = SystemClock.elapsedRealtime();
			Request request;
			try {
				if(mDedicatedDownload == DEDICATED_DOWNLOAD_MIXED)
				{ 
					request = mQueue.take();
					if(!request.take()) {
						continue;
					}
				}
				else if(mDedicatedDownload  == DEDICATED_DOWNLOAD_NORMAL) {
					request = mNormalQueue.take();
					if(!request.take()) {
						continue;
					}
				}
				else {
					request = mOthersQueue.take();
					if(!request.take()) {
						continue;
					}
				}
			} catch (InterruptedException e) {
				// We may have been interrupted because it was time to quit.
				e.printStackTrace();
				if (mQuit)
					return;
				continue;
			}
			
			try {
				request.addMarker("network-queue-take");
				
				// If the request was cancelled already, do not perform the
				// network request.
				if (request.isCanceled()) {
					request.finish("network-discard-cancelled");
					continue;
				}
				
				// Tag the request (if API >= 14)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					TrafficStats
							.setThreadStatsTag(request.getTrafficStatsTag());
				}
				
				request.setStartTime(System.currentTimeMillis());
				// Perform the network request.
				NetworkResponse networkResponse = mNetwork
						.performRequest(request);
				request.addMarker("network-http-complete");
				
				// If the server returned 304 AND we delivered a response
				// already,
				// we're done -- don't deliver a second identical response.
				if (networkResponse.notModified
						&& request.hasHadResponseDelivered()) {
					request.finish("not-modified");
					continue;
				}
				
				// Parse the response here on the worker thread.
				Response<?> response = request
						.parseNetworkResponse(networkResponse);
				request.addMarker("network-parse-complete");
				// Write to cache if applicable.
				// TODO: Only update cache metadata instead of entire record for
				// 304s.
				if (request.shouldCache() && response.cacheEntry != null) {
					mCache.put(request.getCacheKey(), response.cacheEntry);
					request.addMarker("network-cache-written");
				}
				
				// Post the response back.
				request.markDelivered();
				response.setStartTimeInMillis(request.getStartTime());
				mDelivery.postResponse(request, response);
			} catch (VolleyError volleyError) {
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
				parseAndDeliverNetworkError(request, volleyError);
			} catch (Exception e) {
				VolleyLog.e(e, "Unhandled exception %s", e.toString());
                VolleyError volleyError = new VolleyError(e,null);
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                mDelivery.postError(request, volleyError);

			}
		}
	}
	
	private void parseAndDeliverNetworkError(Request<?> request,
			VolleyError error) {
		error = request.parseNetworkError(error);
		mDelivery.postError(request, error);
	}
}
