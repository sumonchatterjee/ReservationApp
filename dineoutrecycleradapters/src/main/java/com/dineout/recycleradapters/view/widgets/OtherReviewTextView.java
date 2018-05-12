package com.dineout.recycleradapters.view.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;

/**
 * Created by sawai.parihar on 21/03/17.
 */

public class OtherReviewTextView extends TextView {
    private static final int DEFAULT_TRIM_LENGTH = 200;
    private static final String ELLIPSIS = ".....";

    private Context mContext;
    private CharSequence originalText;
    private SpannableString trimmedText;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;

    private String appendedText;
    private int appendedTextColor;

    private OtherReviewTextViewCallback mOtherReviewTextViewCallback;

    public interface OtherReviewTextViewCallback {
        void reviewExpandableStatus(boolean isExpanded);
    }

    public OtherReviewTextView(Context context) {
        this(context, null);
    }

    public OtherReviewTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OtherReviewTextView);
        this.trimLength = typedArray.getInt(R.styleable.OtherReviewTextView_trimLength, DEFAULT_TRIM_LENGTH);
        this.appendedText = typedArray.getString(R.styleable.OtherReviewTextView_appendedText);
        this.appendedTextColor = typedArray.getColor(R.styleable.OtherReviewTextView_appendedTextColor, mContext.getResources().getColor(R.color.black));
        typedArray.recycle();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                trim = !trim;
                setText();
                requestFocusFromTouch();

                if (mOtherReviewTextViewCallback != null) {
                    mOtherReviewTextViewCallback.reviewExpandableStatus(!trim);
                }
            }
        });
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimmedText();
        bufferType = type;
        setText();
    }

    private SpannableString getTrimmedText() {
        SpannableString spannableString;

        if (originalText.length() > trimLength) {
            String str = originalText.subSequence(0, trimLength) + appendedText;
            spannableString = new SpannableString(str);
            spannableString.setSpan(new ForegroundColorSpan(appendedTextColor),
                    trimLength, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            spannableString = new SpannableString(originalText);
        }

        return spannableString;
    }

    public CharSequence getOriginalText() {
        return originalText;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        trimmedText = getTrimmedText();
        setText();
    }

    public int getTrimLength() {
        return trimLength;
    }

    public void setOtherReviewTextViewCallback(OtherReviewTextViewCallback otherReviewTextViewCallback) {
        this.mOtherReviewTextViewCallback = otherReviewTextViewCallback;
    }
}
