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

import com.dineout.android.volley.RequestQueue.GlobalRequestQueueListener;

import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing cache triage on a queue of requests.
 *
 * Requests added to the specified cache queue are resolved from cache. Any
 * deliverable response is posted back to the caller via a
 * {@link ResponseDelivery}. Cache misses and responses that require refresh are
 * enqueued on the specified network queue for processing by a
 * {@link NetworkDispatcher}.
 */
@SuppressWarnings("rawtypes")
public class CacheDispatcher extends Thread {

	private static final boolean DEBUG = VolleyLog.DEBUG;

	/** The queue of requests coming in for triage. */
	private final BlockingQueue<Request> mCacheQueue;

	/** The queue of requests going out to the network. */
	private final BlockingQueue<Request> mNetworkQueue;
	private final BlockingQueue<Request> mNormalNetworkQueue;
	private final BlockingQueue<Request> mOtherNetworkQueue;

	/** The cache to read from. */
	private final Cache mCache;

	/** For posting responses. */
	private final ResponseDelivery mDelivery;

	/** Used for telling us to die. */
	private volatile boolean mQuit = false;

	private GlobalRequestQueueListener globalRequestQueueListener;

	/**
	 * Creates a new cache triage dispatcher thread. You must call
	 * {@link #start()} in order to begin processing.
	 *
	 * @param cacheQueue
	 *            Queue of incoming requests for triage
	 * @param networkQueue
	 *            Queue to post requests that require network to
	 * @param cache
	 *            Cache interface to use for resolution
	 * @param delivery
	 *            Delivery interface to use for posting responses
	 */
	public CacheDispatcher(BlockingQueue<Request> cacheQueue,
			BlockingQueue<Request> networkQueue,
			BlockingQueue<Request> normalNetworkQueue,
			BlockingQueue<Request> otherNetworkQueue, Cache cache,
			ResponseDelivery delivery) {
		mCacheQueue = cacheQueue;
		mNetworkQueue = networkQueue;
		mNormalNetworkQueue = normalNetworkQueue;
		mOtherNetworkQueue = otherNetworkQueue;
		mCache = cache;
		mDelivery = delivery;
	}

	public void setGlobalRequestQueueListener(
			GlobalRequestQueueListener globalRequestQueueListener) {
		this.globalRequestQueueListener = globalRequestQueueListener;
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
		if (DEBUG) {
			VolleyLog.v("start new dispatcher");
			// Process.setThreadPriority(Process.thread);
		}

		// Make a blocking call to initialize the cache.
		mCache.initialize();

		while (true) {
			try {
				// Get a request from the cache triage queue, blocking until
				// at least one is available.
				final Request request = mCacheQueue.take();
				request.addMarker("cache-queue-take");

				// If the request has been canceled, don't bother dispatching
				// it.
				if (request.isCanceled()) {
					request.finish("cache-discard-canceled");
					continue;
				}

				if (request.isDeleteCache()) {
					mCache.remove(request.getCacheKey());
					request.finish("cache-discard");
					continue;
				}

				// Attempt to retrieve this item from cache.
				Cache.Entry entry = mCache.get(request.getCacheKey());
				if (entry == null) {
					if (!request.isDeleteCache()) {
						request.addMarker("cache-miss");
						// Cache miss; send off to the network dispatcher.
						addToNetworkQueue(request);
					}
					continue;
				}

				// If it is completely expired, just send it to the network.
				if (entry.isExpired()) {
					request.addMarker("cache-hit-expired");
					request.setCacheEntry(entry);
					addToNetworkQueue(request);
					continue;
				}

				// We have a cache hit; parse its data for delivery back to the
				// request.
				request.addMarker("cache-hit");
				Response<?> response = request
						.parseNetworkResponse(new NetworkResponse(entry.data,
								entry.responseHeaders));
				request.addMarker("cache-hit-parsed");
				response.setCachedResponse(true);
				response.setStartTimeInMillis(request.getStartTime());

				if (!entry.refreshNeeded()) {
					// Completely unexpired cache hit. Just deliver the
					// response.
					mDelivery.postResponse(request, response);
				} else {
					// Soft-expired cache hit. We can deliver the cached
					// response,
					// but we need to also send the request to the network for
					// refreshing.
					request.addMarker("cache-hit-refresh-needed");
					request.setCacheEntry(entry);

					// Mark the response as intermediate.
					response.intermediate = true;
					response.setNetworkRequestSent(true);
					// Post the intermediate response back to the user and have
					// the delivery then forward the request along to the
					// network.
					mDelivery.postResponse(request, response, new Runnable() {

						@Override
						public void run() {
							try {
								addToNetworkQueue(request);
							} catch (InterruptedException e) {
								// Not much we can do about this.
							}
						}
					});
				}

			} catch (InterruptedException e) {
				// We may have been interrupted because it was time to quit.
				if (mQuit)
					return;
				continue;
			}
		}
	}

	private void addToNetworkQueue(Request<?> request)
			throws InterruptedException {
		mNetworkQueue.add(request);
		switch (request.getPriority()) {
			case NORMAL :
				if (mNormalNetworkQueue != null) {
					mNormalNetworkQueue.add(request);
				}
				break;

			default :
				if (mOtherNetworkQueue != null) {
					mOtherNetworkQueue.add(request);
				}
				break;
		}

	}
}
