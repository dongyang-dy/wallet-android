package com.bhex.lib.uikit.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.bhex.lib.uikit.R;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/30
 * Time: 22:17
 */
public class MnemonicInputView extends ViewGroup {

    private List<String> mWordList;

    public MnemonicInputViewChangeListener inputViewChangeListener;

    private List<AppCompatEditText> mListViews;

    private int mItemWidth,mItemHeight;

    private Context mContext;

    private GradientDrawable mWhiteDrawable,mRedDrawable;

    public MnemonicInputView(Context context) {
        this(context,null);
    }

    public MnemonicInputView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MnemonicInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        mContext = context;
        mWhiteDrawable = getRoundRectDrawable(2,
                ColorUtil.getColor(context,R.color.mnemonic_input_bg),0,true,0);

        mRedDrawable = getRoundRectDrawable(2,
                ColorUtil.getColor(context,R.color.mnemonic_input_bg),ColorUtil.getColor(context,R.color.mnemonic_input_error_border),true,1);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //获取子View个数
        int childCount = getChildCount();
        int left =0,top =0,right = 0,bottom =0;
        //Log.d("MnemonicInputView==>:","childCount=="+childCount);
        for(int i = 0;i < childCount; i ++){
            View child = getChildAt(i);
            //测量子View的宽和高
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            left = right+lp.leftMargin;
            top = bottom+lp.topMargin;
            //Log.d("MnemonicInputView==>:",lp+"==top==="+top);
            //Log.d("MnemonicInputView==>:",bottom+"==top==="+top);
            child.layout(left,top,left+mItemWidth,top+mItemHeight);
            right = (lp.leftMargin + mItemWidth + lp.rightMargin)*((i+1)%4);
            bottom = (lp.topMargin+mItemHeight+lp.bottomMargin)*((i+1)/4);
            //Log.d("MnemonicInputView==>:","bottom==="+bottom);
        }
    }

    @Override
    public  LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //计算出所有ChildView的高度和宽度
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        //计算父控件传过来的高度和测量模式
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        mItemWidth = sizeWidth/4-PixelUtils.dp2px(getContext(),16);
        mItemHeight = PixelUtils.dp2px(getContext(),32);
        int childCount = getChildCount();

        int height = childCount/3 * mItemHeight +mItemHeight;

        /*setMeasuredDimension(
                getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));*/
        setMeasuredDimension(sizeWidth,height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode ==KeyEvent.KEYCODE_SPACE){
            Log.d("MnemonicInputView==>","space===");
        }
        return super.onKeyDown(keyCode, event);
    }

    public List<String> getAllMnemonic(){
        List<String> list = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            AppCompatEditText ed = (AppCompatEditText) getChildAt(i);
            String text = ed.getText().toString().trim();
            if(!TextUtils.isEmpty(text)){
                list.add(text);
            }
        }
        return list;
    }

    public void addEditText(String context) {
        int childCount = getChildCount();
        if(childCount>15){
            return;
        }

        //如果之前是空白
        AppCompatEditText editText = (AppCompatEditText)getChildAt(getChildCount()-1);
        if(editText!=null && TextUtils.isEmpty(editText.getText().toString())){
            editText.setText(context);
            return;
        }

        AppCompatEditText et = new AppCompatEditText(getContext());
        et.setTextColor(ContextCompat.getColor(mContext, R.color.global_main_text_color));
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        //et.setImeOptions(EditorInfo.IME_ACTION_DONE);
        et.setSingleLine(true);
        et.setMaxLines(1);
        et.setPadding(PixelUtils.dp2px(mContext,8),0,PixelUtils.dp2px(mContext,8),0);
        //et.setTextCursorDrawable(null);
        et.setOverScrollMode(View.OVER_SCROLL_NEVER);
        et.setGravity(Gravity.CENTER);
        //setEditTextInhibitInputSpace(et);
        et.addTextChangedListener(new MnemonicTextWatch(et));
        et.setOnKeyListener(new MnemonicOnKeyListener(et));
        //setEditTextInhibitInputSpace(et);
        //et.setFocusable(true);//获得焦点
        //et.setFocusableInTouchMode(true);//获得焦点
        if(!TextUtils.isEmpty(context)){
            et.setText(context.trim());
        }

        et.requestFocus();

        if(mWordList.contains(context)){
            et.setBackground(mRedDrawable);
            //et.setBackground(getResources().getDrawable(R.drawable.shape_white_2_corner));
        }else{
            et.setBackground(mWhiteDrawable);
            //et.setBackground(getResources().getDrawable(R.drawable.shape_white_red_2_corner));
        }

        //设置光标
        //et.setCursorVisible(true);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                PixelUtils.dp2px(mContext,32));
        lp.leftMargin = PixelUtils.dp2px(mContext,8);
        lp.topMargin = PixelUtils.dp2px(mContext,8);
        lp.rightMargin = PixelUtils.dp2px(mContext,8);
        lp.bottomMargin = PixelUtils.dp2px(mContext,8);

        addView(et,lp);

        InputMethodManager  inputManager =
                (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et, 0);


        if(inputViewChangeListener!=null){
            inputViewChangeListener.inputViewChange();
        }
    }

    public void removeEditText(AppCompatEditText editText){
        int childCount = getChildCount();
        if(childCount==1){
            return;
        }
        removeView(editText);
        AppCompatEditText et = (AppCompatEditText) getChildAt(getChildCount()-1);
        et.setSelection(et.getText().toString().length());
        et.requestFocus();

        InputMethodManager  inputManager =
                (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et, 0);

        if(inputViewChangeListener!=null){
            inputViewChangeListener.inputViewChange();
        }
    }

    public static void setEditTextInhibitInputSpace(EditText editText){
        InputFilter filter=new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int start, int end, Spanned dest, int dstart, int dend) {
                String regex = "^[\u4E00-\u9FA5]+$";
                boolean isChinese = Pattern.matches(regex, charSequence.toString());
                if (!Character.isLetterOrDigit(charSequence.charAt(start)) || isChinese) {
                    return "";
                }
                return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }


    public class MnemonicTextWatch implements TextWatcher {
        public AppCompatEditText textView;

        public MnemonicTextWatch(AppCompatEditText textView) {
            this.textView = textView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = textView.getText().toString();
            LogUtils.d("MnemonicInputView===>:","text==="+text);
            if(!TextUtils.isEmpty(text)){
                String []array = text.split("\\s+");
                if(array.length>1){
                    textView.setText(array[0]);
                    for (int i = 1; i < array.length; i++) {
                        addEditText(array[i]);
                    }
                }else if(text.endsWith(" ")){
                    textView.setText(text.trim());
                    addEditText(null);
                }
                if(inputViewChangeListener!=null){
                    inputViewChangeListener.inputViewChange();
                }
            }
        }
    }

    public class MnemonicOnKeyListener implements  OnKeyListener{

        public AppCompatEditText textView;

        public MnemonicOnKeyListener(AppCompatEditText textView) {
            this.textView = textView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            Log.d("MnemonicInputView==>","keyCode=="+KeyEvent.keyCodeToString(keyCode));
            /*if(keyCode == KeyEvent.KEYCODE_SPACE){
                addEditText(null);
                //return false;
            }*/

            if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction()==KeyEvent.ACTION_UP){
                String text = textView.getText().toString();
                if(TextUtils.isEmpty(text)&&getChildCount()>1){
                    textView.setFocusable(false);
                    removeEditText(textView);
                }
            }
            return false;
        }
    }

    public void setInputViewStatus(){
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            AppCompatEditText et = (AppCompatEditText)getChildAt(i);
            String context = et.getText().toString().trim();
            if(mWordList.contains(context)){
                et.setBackground(mWhiteDrawable);
                //et.setBackground(getResources().getDrawable(R.drawable.shape_white_2_corner));
            }else{
                et.setBackground(mRedDrawable);
                //et.setBackground(getResources().getDrawable(R.drawable.shape_white_red_2_corner));
            }
        }
    }

    public interface  MnemonicInputViewChangeListener {
        void inputViewChange();
    }

    public MnemonicInputViewChangeListener getInputViewChangeListener() {
        return inputViewChangeListener;
    }

    public void setInputViewChangeListener(MnemonicInputViewChangeListener inputViewChangeListener) {
        this.inputViewChangeListener = inputViewChangeListener;
    }

    public void setWordList(List<String> wordList) {
        this.mWordList = wordList;
    }


    public static GradientDrawable getRoundRectDrawable(int radius, int fillColor, int strokeColor, boolean isFill, int strokeWidth) {
        float[] radius_f = {radius, radius, radius, radius, radius, radius, radius, radius};
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadii(radius_f);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(isFill ? fillColor : Color.TRANSPARENT);
        if(strokeWidth>0){
            drawable.setStroke(strokeWidth, strokeColor);
        }
        return drawable;
    }

    public void importMnemonic(String mnemonic){

        if(getChildCount()==0){
            return;
        }

        for(int i=0;i<getChildCount()-1;i++){
            removeView(getChildAt(i));
        }

        if(getChildCount()==0){
            return;
        }

        AppCompatEditText editText = (AppCompatEditText)getChildAt(0);
        editText.setText(mnemonic);
    }
}
