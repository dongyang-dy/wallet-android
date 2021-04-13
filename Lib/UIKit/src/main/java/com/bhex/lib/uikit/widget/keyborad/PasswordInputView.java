package com.bhex.lib.uikit.widget.keyborad;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.bhex.lib.uikit.R;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.editor.SimpleTextWatcher;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;

/**
 * 密码输入框
 */
public class PasswordInputView extends RelativeLayout {

    private Context mContext;

    private LinearLayout mLlContainer;

    public AppCompatEditText m_input_content;
    //输入框数量
    private int input_count;
    //输入的宽度
    private float input_width;
    //输入框的高度
    private float input_height;

    //是否是密码模式
    private boolean mIsPwd;
    //密码模式时圆的半径
    private float mPwdRadius;
    //
    private float mWidth, input_divider;

    private int input_border_normal_color;
    private int input_border_focus_color;

    private boolean is_border;
    //
    //private boolean is_custom_keyborad;

    public PasswordTextView[] mPasswordTextViews;
    //
    private InputNumberTextWatcher mTextWatcher = new InputNumberTextWatcher();

    private OnInputListener mOnInputListener;

    public PasswordInputView(@NonNull Context context) {
        this(context,null);
    }

    public PasswordInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PasswordInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_password_input, this);
        mLlContainer = findViewById(R.id.ll_container);
        m_input_content = findViewById(R.id.input_content);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasswordInputView, defStyleAttr, 0);
        input_count = typedArray.getInteger(R.styleable.PasswordInputView_h_vcet_number, 4);
        mPwdRadius = typedArray.getDimensionPixelSize(R.styleable.PasswordInputView_h_vcet_pwd_radius, PixelUtils.dp2px(context,5));
        input_width = typedArray.getDimensionPixelSize(R.styleable.PasswordInputView_h_vcet_width, PixelUtils.dp2px(context,20));
        input_height = typedArray.getDimensionPixelSize(R.styleable.PasswordInputView_h_vcet_height, PixelUtils.dp2px(context,20));
        input_divider = typedArray.getDimensionPixelSize(R.styleable.PasswordInputView_h_vcet_divider, PixelUtils.dp2px(context,8));

        input_border_normal_color  = typedArray.getColor(R.styleable.PasswordInputView_input_border_normal_color,
                ContextCompat.getColor(mContext,R.color.global_input_hint_color));

        input_border_focus_color = typedArray.getColor(R.styleable.PasswordInputView_input_border_focus_color,
                ContextCompat.getColor(mContext,R.color.blue_bg));

        is_border = typedArray.getBoolean(R.styleable.PasswordInputView_h_vcet_is_border, false);

        //is_custom_keyborad = typedArray.getBoolean(R.styleable.PasswordInputView_h_vcet_custom_keyborad, false);
        typedArray.recycle();
        initUI();
    }

    private void initUI() {
        if(input_height==0) input_height = input_width;
        initListener();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置当 高为 warpContent 模式时的默认值 为 50dp
        int heightMeasureSpecValue = heightMeasureSpec;
        int heightMode = MeasureSpec.getMode(heightMeasureSpecValue);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpecValue = MeasureSpec.makeMeasureSpec(PixelUtils.dp2px(getContext(),50), MeasureSpec.EXACTLY);
        }

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        initTextViews();
        super.onMeasure(widthMeasureSpec, heightMeasureSpecValue);
    }

    //
    public void initTextViews(){
        if(input_count<=0){
            return;
        }

        GradientDrawable input_border_normal_color_drawable = ShapeUtils.getRoundRectStrokeDrawable(
                4, ContextCompat.getColor(mContext,R.color.app_bg),input_border_normal_color,2
        );

        //input_width = (float)((mWidth-(input_count-1)*input_divider))/input_count;
        //input_divider = (float) (mWidth-input_count*input_width)/(input_count-1);
        m_input_content.setCursorVisible(false);
        //m_input_content.setEnabled(!is_custom_keyborad);

        m_input_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(input_count)});
        mPasswordTextViews = new PasswordTextView[input_count];
        mLlContainer.removeAllViews();
        //计算间距
        for (int i = 0; i < mPasswordTextViews.length; i++) {
            PasswordTextView pwdTextView = new PasswordTextView(mContext);
            pwdTextView.is_border = is_border;
            pwdTextView.mRadius = mPwdRadius;

            pwdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            pwdTextView.input_border_focus_color = input_border_focus_color;
            pwdTextView.input_border_normal_color = input_border_normal_color;
            pwdTextView.is_border = is_border;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)input_width,(int)input_height);
            if(i<mPasswordTextViews.length-1){
                lp.rightMargin = (int)input_divider;
            }
            lp.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;
            pwdTextView.mHasPassword = false;
            pwdTextView.setLayoutParams(lp);
            pwdTextView.setFocusable(false);
            pwdTextView.is_border = is_border;
            if(is_border){
                pwdTextView.input_border_normal_color_drawable = input_border_normal_color_drawable;
                pwdTextView.setBackground(input_border_normal_color_drawable);
            }
            mPasswordTextViews[i] = pwdTextView;
            mLlContainer.addView(mPasswordTextViews[i],i);
        }
    }


    private void initListener() {
        m_input_content.addTextChangedListener(mTextWatcher);
        m_input_content.setOnKeyListener((v, keyCode, event) -> {
            //LogUtils.d("PasswordInputView===>:","v.class==="+v.getClass().getName());
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                onKeyDelete();
                return true;
            }
            return false;
        });
    }

    public void onKeyDelete() {
        for (int i = mPasswordTextViews.length - 1; i >= 0; i--) {
            PasswordTextView tv = mPasswordTextViews[i];
            if (!TextUtils.isEmpty(tv.getText())) {
                tv.clearPassword();
                mPasswordTextViews[i].setText("");
                break;
            }
        }
        changeTextAction();
    }

    public void clearInputContent() {
        for (int i = mPasswordTextViews.length - 1; i >= 0; i--) {
            PasswordTextView tv = mPasswordTextViews[i];
            if (!TextUtils.isEmpty(tv.getText())) {
                tv.clearPassword();
                mPasswordTextViews[i].setText("");
            }
        }
        changeTextAction();
    }

    private class InputNumberTextWatcher extends SimpleTextWatcher {
        @Override
        public void afterTextChanged(Editable editable) {
            onInputChange(editable);
        }
    }

    public void setText(String inputContent){
        if(TextUtils.isEmpty(inputContent)){
            return;
        }

        for(int i = 0; i < mPasswordTextViews.length; i++) {
            PasswordTextView tv = mPasswordTextViews[i];
            if(!TextUtils.isEmpty(tv.getText())){
                continue;
            }
            tv.drawPassword();
            mPasswordTextViews[i].setText(inputContent);
            break;
        }
        changeTextAction();
    }

    public void onInputChange(Editable editable){
        String inputStr = editable.toString();
        if(TextUtils.isEmpty(inputStr)){
            return;
        }
        String[] strArray = inputStr.split("");
        for(int i = 0; i < strArray.length; i++){
            if (i > input_count) {
                break;
            }
            setText(strArray[i]);
            m_input_content.setText("");
        }
    }

    public String getInputContent(){
        StringBuffer result = new StringBuffer("");
        for(int i=0;i<mPasswordTextViews.length;i++){
            PasswordTextView passwordTextView = mPasswordTextViews[i];
            result.append(passwordTextView.getText());
        }
        return result.toString();
    }

    public void setOnInputListener(OnInputListener onInputListener) {
        mOnInputListener = onInputListener;
    }

    public void changeTextAction(){
        String result = getInputContent();
        if(TextUtils.isEmpty(result)){
            if (mOnInputListener != null) {
                mOnInputListener.onClear();
            }
        } else if(result.length()<input_count){
            if (mOnInputListener != null) {
                mOnInputListener.onChange(getInputContent());
            }
        }else{
            if (mOnInputListener != null) {
                mOnInputListener.onComplete(getInputContent());
            }
        }
    }
    //输入框监听
    public interface OnInputListener {
        //输入完成
        void onComplete(String input);
        //输入变化
        void onChange(String input);
        //输入清空
        void onClear();
    }
}
