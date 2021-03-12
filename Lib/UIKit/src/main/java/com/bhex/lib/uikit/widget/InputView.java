package com.bhex.lib.uikit.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

import com.bhex.lib.uikit.R;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.google.android.material.shape.MaterialShapeUtils;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/10
 * Time: 20:17
 */
public class InputView extends LinearLayout implements View.OnClickListener {

    private AppCompatEditText mInputEd;
    private AppCompatCheckBox mInputShow;
    private AppCompatImageView mInputClear;

    public final static int SILENTMODE = 0;
    public final static int NORMALMODE = 1;
    public final static int PWDMODE = 2;
    public final static int NUMBERPWDMODE = 3;
    private int mMode = NORMALMODE;
    private int mNum = 6;
    //错误提示
    private TextView errorTxt;
    private LinearLayout mInputAction;
    private RelativeLayout layout_content;
    private String hint="";
    private boolean editable;

    private Context mContext;

    private InputCheckListener mListener;


    public InputView(Context context) {
        this(context,null);
    }

    public InputView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public InputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.InputView);
        hint = typedArray.getString(R.styleable.InputView_hint);
        editable = typedArray.getBoolean(R.styleable.InputView_editable,true);
        mMode = typedArray.getInt(R.styleable.InputView_textType,-1);
        LayoutInflater.from(context).inflate(R.layout.view_input_edittext_layout,
                this, true);
        setOrientation(HORIZONTAL);

        initView();

        addEvent();
        setInputMode(mMode);
    }

    private void initView() {
        mInputEd = findViewById(R.id.input_edit);
        setInputHint(hint);
        setInputNoEditable(editable);
        errorTxt = findViewById(R.id.error_text);

        mInputShow = findViewById(R.id.input_show);

        mInputAction = findViewById(R.id.input_action);
        mInputClear = findViewById(R.id.input_clear);

        layout_content = findViewById(R.id.layout_content);
    }

    private void addEvent() {
        mInputClear.setOnClickListener(this);

        mInputShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(mMode==NUMBERPWDMODE){
                        mInputEd.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD|InputType.TYPE_CLASS_NUMBER);
                        mInputEd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }else{
                        mInputEd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                } else {
                    if(mMode==NUMBERPWDMODE){
                        mInputEd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }else{
                        mInputEd.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }

                if (mMode==PWDMODE || mMode==NUMBERPWDMODE) {
                    checkPasswdFont(mInputEd.getText().toString());
                    mInputEd.setSelection(mInputEd.getText().length());
                }
            }
        });

        mInputEd.addTextChangedListener(new InputChangeListener());
    }

    public void setInputMode(int mode) {
        mMode = mode;
        switch (mode) {
            case SILENTMODE:
                mInputShow.setVisibility(View.GONE);
                mInputClear.setVisibility(View.GONE);
                break;
            case NORMALMODE:
                mInputEd.setInputType(InputType.TYPE_CLASS_PHONE);
                mInputShow.setVisibility(View.GONE);
                break;
            case PWDMODE:
                mInputShow.setVisibility(View.VISIBLE);
                mInputEd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                mInputEd.setTypeface(Typeface.SANS_SERIF);
                break;
            case NUMBERPWDMODE:
                mInputShow.setVisibility(View.VISIBLE);
                mInputEd.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD|InputType.TYPE_CLASS_NUMBER);
                mInputEd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mInputEd.setTypeface(Typeface.SANS_SERIF);
                break;
            default:
                break;
        }
    }


    public void setInputHint(String hint) {
        mInputEd.setHint(hint);
    }


    private void setInputNoEditable(boolean editable) {
        mInputEd.setEnabled(editable);
        mInputEd.setTextColor(ColorUtil.getColor(mContext,R.color.global_input_text_color));
    }


    /**
     * 设置输入框内左边距
     * @param left
     */
    public void setPaddingLeft(int left){
        mInputEd.setPadding(left,mInputEd.getPaddingTop(),mInputEd.getPaddingRight(),mInputEd.getPaddingBottom());
    }

    /**
     * 设置输入框内右边距
     * @param right
     */
    public void setPaddingRight(int right){
        int inputActionWidth = PixelUtils.dp2px(mContext,30);
        mInputEd.setPadding(mInputEd.getPaddingLeft(),mInputEd.getPaddingTop(),right+inputActionWidth,mInputEd.getPaddingBottom());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mInputAction.getLayoutParams();
        layoutParams.rightMargin = right+ PixelUtils.dp2px(mContext,5);

        mInputAction.setLayoutParams(layoutParams);
    }

    /**
     * 提示错误
     * @param error
     */
    public void setError(String error) {
        if (TextUtils.isEmpty(error)||TextUtils.isEmpty(mInputEd.getText().toString())) {
            errorTxt.setText("");
            errorTxt.setVisibility(GONE);
            //
            Drawable drawable = ShapeUtils.getRoundRectStrokeDrawable(4,
                    ColorUtil.getColor(mContext,R.color.global_input_background),
                    ColorUtil.getColor(mContext,R.color.bg_trans),2);
            layout_content.setBackground(drawable);
        }else{
            errorTxt.setVisibility(VISIBLE);
            errorTxt.setText(error);
            Drawable drawable = ShapeUtils.getRoundRectStrokeDrawable(4,
                    ColorUtil.getColor(mContext,R.color.global_input_background),
                    ColorUtil.getColor(mContext,R.color.input_error),2);
            layout_content.setBackground(drawable);
        }
    }

    public TextView getErrorTextView() {
        return errorTxt;
    }

    /**
     * 设置焦点改变监听
     * @param listener
     */
    public void setOnFocusChangeListener(OnFocusChangeListener listener){
        mInputEd.setOnFocusChangeListener(listener);
    }

    public void addTextWatch(TextWatcher mTextWatcher) {
        mInputEd.addTextChangedListener(mTextWatcher);
    }

    public EditText getEditText() {
        return mInputEd;
    }

    public void setInputEditFocusable(boolean isFocus) {
        if (mInputEd != null) {
            mInputEd.setFocusable(isFocus);
//            mInputEd.setSelection(mInputEd.getText().toString().length());
            mInputEd.setPressed(true);
        }
    }

    private class InputChangeListener implements TextWatcher {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @SuppressLint("NewApi")
        @Override
        public void afterTextChanged(Editable edit) {
            if (mListener != null && edit.toString().length() != mNum) {
                mListener.checkStatus();
            }
            if (mMode == SILENTMODE){
                return;
            }else{
                if (TextUtils.isEmpty(edit.toString())||!editable) {
                    mInputClear.setVisibility(View.GONE);
                } else {
                    mInputClear.setVisibility(View.VISIBLE);
                }
            }

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
            if (TextUtils.isEmpty(arg0)) {
                errorTxt.setVisibility(GONE);
            }else{
            }

            if (mMode==PWDMODE||mMode==NUMBERPWDMODE) {
                checkPasswdFont(arg0);
            }
        }
    }

    /**
     * 改变密码字体
     * @param arg0
     */
    private void checkPasswdFont(CharSequence arg0) {
        Typeface typeface = mInputEd.getTypeface();
        if (TextUtils.isEmpty(arg0)) {
            if (typeface==null||!typeface.equals(Typeface.SANS_SERIF)) {
                mInputEd.setTypeface(Typeface.SANS_SERIF);
            }
        }else{
            if (mInputShow.isChecked()) {
                if (typeface==null||!typeface.equals(Typeface.SANS_SERIF)) {
                    mInputEd.setTypeface(Typeface.SANS_SERIF);
                }
            }else{
                if (typeface==null||!typeface.equals(Typeface.MONOSPACE)) {
                    mInputEd.setTypeface(Typeface.MONOSPACE);
                }
            }
        }

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.input_clear) {
            mInputEd.setText("");
            mInputClear.setVisibility(View.GONE);
        }
    }

    public String getInputString() {
        return mInputEd.getText().toString().trim();
    }

    public void setInputString(String text) {
        mInputEd.setText(text);

    }

    public String getInputStringNoTrim() {
        return mInputEd.getText().toString();
    }

    public interface InputCheckListener {
        void checkStatus();
    }
}
