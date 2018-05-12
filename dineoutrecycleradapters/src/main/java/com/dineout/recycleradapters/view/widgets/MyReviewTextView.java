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

public class MyReviewTextView extends TextView {
    private static final int DEFAULT_TRIM_LENGTH = 200;
    private static final String APPEND_TEXT = "Edit review";

    private Context mContext;
    private CharSequence originalText;
    private SpannableString trimmedText;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;

    private String appendedText;
    private int appendedTextColor;

    public MyReviewTextView(Context context) {
        this(context, null);
    }

    public MyReviewTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OtherReviewTextView);
        this.trimLength = typedArray.getInt(R.styleable.OtherReviewTextView_trimLength, DEFAULT_TRIM_LENGTH);
        this.appendedText = typedArray.getString(R.styleable.OtherReviewTextView_appendedText);
        this.appendedTextColor = typedArray.getColor(R.styleable.OtherReviewTextView_appendedTextColor, mContext.getResources().getColor(R.color.black));
        typedArray.recycle();
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return (trim ? trimmedText : originalText);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = getOriginalText(text);
        trimmedText = getTrimmedText();
        bufferType = type;
        setText();
    }

    private SpannableString getOriginalText(CharSequence text) {
        String str = text + " " + APPEND_TEXT;
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ForegroundColorSpan(appendedTextColor),
                text.length(), str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private SpannableString getTrimmedText() {
        SpannableString spannableString;

        if (originalText.length() > trimLength) {
            String str = originalText.subSequence(0, trimLength) + appendedText + " " + APPEND_TEXT;
            spannableString = new SpannableString(str);
            spannableString.setSpan(new ForegroundColorSpan(appendedTextColor),
                    trimLength, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            spannableString = new SpannableString(originalText);
        }

        return spannableString;
    }
}
