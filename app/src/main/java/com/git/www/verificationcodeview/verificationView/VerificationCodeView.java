package com.git.www.verificationcodeview.verificationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.git.www.verificationcodeview.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jingzheng on 2017/9/5.
 */


public class VerificationCodeView extends LinearLayoutCompat implements TextWatcher, View.OnKeyListener {


    public int blockSize;
    private int textsize;
    private int boxHeight;
    private int boxWidth;
    private int boxSpace;
    private Context context;
    List<EditText> container = new ArrayList<>();
    private StringBuffer sb = new StringBuffer();
    private EditText editText;
    private LayoutParams params;
    private Drawable focusDrawable;
    private Drawable unfocusDrawable;
    private TextCompleteListener listener;

    public VerificationCodeView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public VerificationCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public VerificationCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VerificationCodeView, defStyleAttr, defStyleAttr);

        blockSize = typedArray.getInt(R.styleable.VerificationCodeView_vcv_box_size, blockSize);
        textsize = typedArray.getLayoutDimension(R.styleable.VerificationCodeView_vcv_textsize, textsize);
        boxHeight = typedArray.getLayoutDimension(R.styleable.VerificationCodeView_vcv_box_height, boxHeight);
        boxWidth = typedArray.getLayoutDimension(R.styleable.VerificationCodeView_vcv_box_width,boxWidth);


        typedArray.recycle();

        initView();

    }


    private void initView() {
        focusDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.verification_bg_focus, null);
        unfocusDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.verification_bg_normal, null);
        params = new LayoutParams(boxWidth, boxHeight, 1f);
        for (int i = 0; i < blockSize; i++) {
            ViewCompat.setLayoutDirection(this, HORIZONTAL);
            editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setGravity(Gravity.CENTER);
            editText.setTextColor(Color.BLACK);
            InputFilter[] array = new InputFilter[1];
            array[0] = new InputFilter.LengthFilter(1);
            editText.setFilters(array);
            editText.setTextSize(textsize);
            editText.addTextChangedListener(this);
            editText.setOnKeyListener(this);

            container.add(editText);
            addView(editText);

        }
    }


    int count = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (count == 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);//父窗体宽度测量
            boxSpace = (width - boxWidth * container.size())/(container.size()-1);
            count++;
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            EditText child = (EditText) getChildAt(i);
            params.leftMargin = i == 0 ? 0 : boxSpace;
            child.setLayoutParams(params);

        }
        checkBackground();
    }

    private void checkEdit() {
        for (int i = 0; i < container.size(); i++) {
            EditText item = container.get(i);
            boolean isFocus = item.isFocusable();
            Editable text = item.getText();
            if (!TextUtils.isEmpty(text) && i < container.size() - 1 && isFocus && TextUtils.isEmpty(container.get(i + 1).getText())) {
                container.get(i + 1).requestFocus();
            }
        }
        checkBackground();


    }

    private void checkBackground() {
        for (int i = 0; i < container.size(); i++) {
            EditText item = container.get(i);
            String input = item.getText().toString();
            boolean isEmpty = TextUtils.isEmpty(input);
            item.setBackground(isEmpty ? unfocusDrawable : focusDrawable);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkEdit();

    }

    @Override
    public void afterTextChanged(Editable s) {

        for (int i = 0; i < container.size(); i++) {
            if (TextUtils.isEmpty(container.get(i).getText())) {
                return;
            }
        }
        for (int i = 0; i < container.size(); i++) {
            sb.append(container.get(i).getText());
        }
        if (listener != null && sb.length() > 0) {
            listener.onComplete(sb.toString());
        }

    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        EditText edit = (EditText) v;
        if (keyCode == KeyEvent.KEYCODE_DEL && TextUtils.isEmpty(edit.getText()) && !edit.equals(getChildAt(0))) {
            edit.setText(null);

            for (int i = 0; i < container.size(); i++) {
                EditText item = container.get(i);
                if (item.equals(edit)) {
                    container.get(i - 1).requestFocus();
                }
            }
            checkBackground();

        }
        return false;
    }

    interface TextCompleteListener {
        void onComplete(String text);
    }

    public void setVerificationListener(TextCompleteListener listener) {
        this.listener = listener;
    }

    public void clearText() {
        for (int i = 0; i < container.size(); i++) {
            EditText item = container.get(i);
            item.setText(null);
        }
        container.get(0).requestFocus();
        checkBackground();
    }

}