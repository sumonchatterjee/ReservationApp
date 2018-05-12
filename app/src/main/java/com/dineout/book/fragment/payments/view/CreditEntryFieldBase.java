package com.dineout.book.fragment.payments.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

public abstract class CreditEntryFieldBase extends EditText implements
        TextWatcher, OnKeyListener, OnClickListener, View.OnTouchListener, View.OnLongClickListener {

    final Context context;
    com.dineout.book.fragment.payments.view.CreditCardFieldDelegate delegate;
    String lastValue = null;

    private boolean valid = false;

    public CreditEntryFieldBase(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CreditEntryFieldBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public CreditEntryFieldBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs);
    }

    void init() {
        init(null);
    }

    void init(AttributeSet attrs) {
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        addTextChangedListener(this);
        setOnKeyListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);

    }

    void setStyle(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }


    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int end) {

        String tmp = String.valueOf(s);
        if (!tmp.equals(lastValue)) {
            lastValue = tmp;
            textChanged(s, start, before, end);
        }

    }

    public abstract void formatAndSetText(String updatedString, int start);

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public void textChanged(CharSequence s, int start, int before, int end) {
    }

//	@Override
//	public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
//		outAttrs.actionLabel = null;
//		outAttrs.inputType = InputType.TYPE_NULL;
//		outAttrs.imeOptions = EditorInfo.IME_ACTION_NONE;
////		return new BackInputConnection(super.onCreateInputConnection(outAttrs));
//	}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            return false;
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT
                || keyCode == KeyEvent.KEYCODE_ALT_RIGHT
                || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
                || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
            return false;

        if (keyCode == KeyEvent.KEYCODE_DEL
                && this.getText().toString().length() == 0) {
            if (delegate != null) {
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {


    }

    @SuppressWarnings("unused")
    public com.dineout.book.fragment.payments.view.CreditCardFieldDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(com.dineout.book.fragment.payments.view.CreditCardFieldDelegate delegate) {
        this.delegate = delegate;
    }


    @Override
    public boolean onLongClick(View v) {
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    public abstract String helperText();

    public boolean isValid() {
        return valid;
    }

    void setValid(boolean valid) {
        this.valid = valid;
    }

    private void backInput() {
        if (this.getText().toString().length() == 0) {
            if (delegate != null) {
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putBoolean("focus", hasFocus());
        bundle.putString("stateToSave", String.valueOf(this.getText()));
        return bundle;
    }


    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("instanceState");
            super.onRestoreInstanceState(state);
            String cc = bundle.getString("stateToSave");
            setText(cc);
            boolean focus = bundle.getBoolean("focus", false);
            if (focus) requestFocus();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public void setCursorDrawableColor(int color) {
        //http://stackoverflow.com/questions/25996032/how-to-change-programatically-edittext-cursor-color-in-android
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(this);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(this);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(getContext(), mCursorDrawableRes);
            drawables[1] = ContextCompat.getDrawable(getContext(), mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (final Throwable ignored) {
            //
        }
    }

    private class BackInputConnection extends InputConnectionWrapper {
        public BackInputConnection(InputConnection target) {
            super(target, false);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {

            return super.sendKeyEvent(event);
        }

        // From Android 4.1 this is called when the DEL key is pressed on the
        // soft keyboard (and
        // sendKeyEvent() is not called). We convert this to a "normal" key
        // event.
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {

            if (android.os.Build.VERSION.SDK_INT < 11) {
                return super.deleteSurroundingText(beforeLength, afterLength);
            } else {

                long eventTime = SystemClock.uptimeMillis();

                int flags = KeyEvent.FLAG_SOFT_KEYBOARD
                        | KeyEvent.FLAG_KEEP_TOUCH_MODE
                        | KeyEvent.FLAG_EDITOR_ACTION;

                sendKeyEvent(new KeyEvent(eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags));

                sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(),
                        eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0,
                        0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags));
                return true;
            }
        }
    }

}
