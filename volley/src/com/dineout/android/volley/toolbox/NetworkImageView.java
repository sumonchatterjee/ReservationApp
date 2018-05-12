/**
 * Copyright (C) 2013 The Android Open Source Project
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
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.ImageLoader.ImageContainer;
import com.dineout.android.volley.toolbox.ImageLoader.ImageListener;
import com.dineout.android.volley.toolbox.ImageLoader.ImageUrl;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class NetworkImageView extends ImageView {

	/** The URL of the network image to load */
	private ImageUrl mUrl;
	private boolean hasLocalBitmap;
	private boolean defaultImageSet;
	/**
	 * Resource ID of the image to be used as a placeholder until the network
	 * image is loaded.
	 */
	private int mDefaultImageId;

	/**
	 * Resource ID of the image to be used if the network response fails.
	 */
	private int mErrorImageId;

	/** Local copy of the ImageLoader. */
	private ImageLoader mImageLoader;

	/** Current ImageContainer. (either in-flight or finished) */
	private ImageContainer mImageContainer;

    public static final int VIEW_ANIMATION_SCALING = 1001;

    public static final int SCALE_DURATION = 500;

    public static final float MIN_SCALE_VAL = 0.9f;

    public static final float MAX_SCALE_VAL = 1.0f;

    private boolean mShouldAnimate = false;

    private int mAnimationType;

	public NetworkImageView(Context context) {
		this(context, null);
	}

	public NetworkImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setHasLocalBitmap(boolean hasLocalBitmap) {
		this.hasLocalBitmap = hasLocalBitmap;
	}

	/**
	 * Sets URL of the image that should be loaded into this view. Note that
	 * calling this will immediately either set the cached image (if available)
	 * or the default image specified by
	 * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
	 *
	 * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)}
	 * and {@link NetworkImageView#setErrorImageResId(int)} should be called
	 * prior to calling this function.
	 *
	 * @param url
	 *            The URL that should be loaded into this ImageView.
	 * @param imageLoader
	 *            ImageLoader that will be used to make the request.
	 */
	public void setImageUrl(String url, ImageLoader imageLoader) {
		setImageUrl(new ImageUrl(url), imageLoader);
	}

	public void setImageUrl(ImageUrl imageUrl, ImageLoader imageLoader) {
		mUrl = imageUrl;
		mImageLoader = imageLoader;
		loadImageIfNecessary(false);
	}

	public void setImageUrl(String url, List<String> resolutionsHighToLow,
			ImageLoader imageLoader) {
		if (resolutionsHighToLow == null || TextUtils.isEmpty(url)) {
			setImageUrl(url, imageLoader);
			return;
		}

		List<ImageUrl> resolutionsImageUrl = new ArrayList<ImageUrl>();
		String replaceKey = null;
		for (String resolution : resolutionsHighToLow) {
			if (url.contains(resolution)) {
				replaceKey = resolution;
				break;
			}
		}

		if (!TextUtils.isEmpty(replaceKey)) {
			for (String resolution : resolutionsHighToLow) {
				String[] split = resolution.split("x");
				ImageUrl imageUrl;
				if (resolution.equalsIgnoreCase("large")) {
					imageUrl = new ImageUrl(url.replaceFirst(replaceKey,
							"large"), null, 310, 360);
				} else {
					imageUrl = new ImageUrl(url.replaceFirst(replaceKey,
							resolution), null, Integer.parseInt(split[0]),
							Integer.parseInt(split[1]));
				}

				resolutionsImageUrl.add(imageUrl);
			}

			String[] split = null;

			if (replaceKey.equals("large")) {
				split = new String[]{"310", "360"};
			} else {
				split = replaceKey.split("x");
			}

			if (split.length <= 1) {
				split = new String[]{"100", "100"};
			}

			ImageUrl iurl = new ImageUrl(url, resolutionsImageUrl,
					Integer.parseInt(split[0]), Integer.parseInt(split[1]));
			setImageUrl(iurl, imageLoader);
		} else {
			setImageUrl(url, imageLoader);
		}
	}

	/**
	 * Sets the default image resource ID to be used for this view until the
	 * attempt to load it completes.
	 */
	public void setDefaultImageResId(int defaultImage) {
		mDefaultImageId = defaultImage;
	}

	/**
	 * Sets the error image resource ID to be used for this view in the event
	 * that the image requested fails to load.
	 */
	public void setErrorImageResId(int errorImage) {
		mErrorImageId = errorImage;
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		setDefaultImageResId(resId);
		this.defaultImageSet = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable currentDrawable = getDrawable();

		if (currentDrawable instanceof TransitionDrawable) {
			currentDrawable = ((TransitionDrawable) currentDrawable)
					.getDrawable(1);
		}

		if (currentDrawable instanceof BitmapDrawable) {
			BitmapDrawable drawable = (BitmapDrawable) currentDrawable;
			if (drawable != null && drawable.getBitmap() != null
					&& drawable.getBitmap().isRecycled()) {
				setImageBitmap(null);
				mImageContainer.cancelRequest();
				mImageContainer = null;
				setImageUrl(mUrl, mImageLoader);
				return;
			}
		}
		super.onDraw(canvas);
	}

	/**
	 * Loads the image for the view if it isn't already loaded.
	 *
	 * @param isInLayoutPass
	 *            True if this was invoked from a layout pass, false otherwise.
	 */
	void loadImageIfNecessary(final boolean isInLayoutPass) {
		int width = getWidth();
		int height = getHeight();

		boolean wrapWidth = false, wrapHeight = false;
		if (getLayoutParams() != null) {
			wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
			wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
		}

		// if the view's bounds aren't known yet, and this is not a
		// wrap-content/wrap-content
		// view, hold off on loading the image.
		boolean isFullyWrapContent = wrapWidth && wrapHeight;
		if (width == 0 && height == 0 && !isFullyWrapContent)
			return;

		// if the URL to be loaded in this view is empty, cancel any old
		// requests and clear the
		// currently loaded image.
		if (mUrl == null || TextUtils.isEmpty(mUrl.url)) {
			if (mImageContainer != null) {
				mImageContainer.cancelRequest();
				mImageContainer = null;
			}
			setDefaultImageOrNull();
			return;
		}

		// if there was an old request in this view, check if it needs to be
		// canceled.
		if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
			if (mImageContainer.getRequestUrl().equals(mUrl.url))
				// if the request is from the same URL, return.
				return;
			else {
				// if there is a pre-existing request, cancel it if it's
				// fetching a different URL.
				mImageContainer.cancelRequest();
				setDefaultImageOrNull();
			}
		}

		// Calculate the max image width / height to use while ignoring
		// WRAP_CONTENT dimens.
		int maxWidth = wrapWidth ? 0 : width;
		int maxHeight = wrapHeight ? 0 : height;

		// The pre-existing content of this view didn't match the current URL.
		// Load the new image
		// from the network.

		ImageContainer newContainer = mImageLoader.get(mUrl,
				new ImageListener() {

					@Override
					public void onErrorResponse(Request request,
							VolleyError error) {
						if (mErrorImageId != 0) {
							setImageResource(mErrorImageId);
						}
					}

					@Override
					public void onResponse(final ImageContainer response,
							boolean isImmediate) {
						// If this was an immediate response that was delivered
						// inside of a layout
						// pass do not set the image immediately as it will
						// trigger a requestLayout
						// inside of a layout. Instead, defer setting the image
						// by posting back to
						// the main thread.
						if (isImmediate && isInLayoutPass) {
							post(new Runnable() {

								@Override
								public void run() {
									onResponse(response, false);
								}
							});
							return;
						}
						if (mUrl != null && response.getBitmap() != null) {
							if (response.getRequestUrl().equals(
									mUrl.originalUrl)) {
								setImageBitmap(response.getBitmap());
                                // adding support for animation
                                if (mShouldAnimate) {
                                  setImageAnimation();
                                }
							} else if (mUrl.otherResolutionUrlslist != null) {
								String url = response.getRequestUrl();
								for (ImageUrl iurl : mUrl.otherResolutionUrlslist) {
									if (iurl.url.equals(url)) {
										setImageBitmap(response.getBitmap());
										break;
									}
								}
							}
						} else if (mDefaultImageId != 0) {
							setImageResource(mDefaultImageId);
						}
					}
				}, maxWidth, maxHeight, false);

		// update the ImageContainer to be the new bitmap container.
		mImageContainer = newContainer;
	}

    /**
     * set Image animation based on mAnimationType
     */
    private void setImageAnimation() {
        switch (mAnimationType) {
            case VIEW_ANIMATION_SCALING:
                setScaleAnimation();
                break;
            default:
                break;
        }

    }

	/**
     * set scale animation
     */
    private void setScaleAnimation() {
        this.setScaleX(MIN_SCALE_VAL);
        this.setScaleY(MIN_SCALE_VAL);
        this.animate().scaleX(MAX_SCALE_VAL).setDuration(SCALE_DURATION);
        this.animate().scaleY(MAX_SCALE_VAL).setDuration(SCALE_DURATION);
    }

    private void setDefaultImageOrNull() {
		if (mDefaultImageId != 0) {
			setImageResource(mDefaultImageId);
		} else if (!hasLocalBitmap) {
			setImageBitmap(null);
		}
	}

	// @Override
	// public void setImageBitmap(Bitmap bm) {

	// if (bm != null) {
	// Drawable transitionFromDrawable = null;
	// Drawable drawable = getDrawable();
	// if (!defaultImageSet && drawable != null) {
	// if (drawable instanceof TransitionDrawable) {
	// Drawable drawable2 = ((TransitionDrawable) drawable)
	// .getDrawable(1);
	// if (drawable2 instanceof BitmapDrawable) {
	// Bitmap bitmap = ((BitmapDrawable) drawable2)
	// .getBitmap();
	// if (bitmap != null && !bitmap.isRecycled()) {
	// transitionFromDrawable = drawable2;
	// }
	// }
	// }
	// }
	//
	// if (transitionFromDrawable == null) {
	// transitionFromDrawable = new ColorDrawable(
	// android.R.color.transparent);
	// }
	//
	// TransitionDrawable td = new TransitionDrawable(new Drawable[]{
	// transitionFromDrawable,
	// new BitmapDrawable(getContext().getResources(), bm)});
	// defaultImageSet = false;
	// setImageDrawable(td);
	// td.startTransition(400);
	// } else
	// super.setImageBitmap(bm);
	// }

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		loadImageIfNecessary(true);
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mImageContainer != null) {
			// If the view was bound to an image request, cancel it and clear
			// out the image from the view.
			mImageContainer.cancelRequest();
			setImageBitmap(null);
			// also clear out the container so we can reload the image if
			// necessary.
			mImageContainer = null;
		}
		super.onDetachedFromWindow();
	}


	@Override
	protected void onAttachedToWindow() {
		loadImageIfNecessary(false);
		super.onAttachedToWindow();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

    /**
     * set animation flag and type to provide support for animation while images are loaded
     * @param shouldAnimate
     * @param animationType
     */
    protected void setShouldAnimate(boolean shouldAnimate, int animationType) {
        this.mShouldAnimate = shouldAnimate;
        this.mAnimationType = animationType;
    }
}
