package com.bhex.wallet.mnemonic.adapter;

import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.bhex.lib.uikit.widget.MnemonicTextViewExt;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.ui.item.MnemonicItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/6
 * Time: 20:54
 */
public class AboveMnemonicAdapter extends BaseQuickAdapter<MnemonicItem, BaseViewHolder> {

    private List<MnemonicItem> mOriginList;

    public AboveMnemonicAdapter(@Nullable List<MnemonicItem> data,@Nullable List<MnemonicItem> originList) {
        super(R.layout.item_mnemonic_above, data);
        mOriginList = originList;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable MnemonicItem memonicItem) {
        MnemonicTextViewExt mnemonicTextView =  viewHolder.getView(R.id.mtv_word);
        int position = viewHolder.getPosition();


        if(position==0){
            mnemonicTextView.getRootLayout().setBackground(getContext().getDrawable(R.drawable.btn_white_top_left_corner));
        }else if(position==2){
            mnemonicTextView.getRootLayout().setBackground(getContext().getDrawable(R.drawable.btn_white_top_right_corner));
        }else if(position==9){
            mnemonicTextView.getRootLayout().setBackground(getContext().getDrawable(R.drawable.btn_white_bottom_left_corner));
        }else if(position==11){
            mnemonicTextView.getRootLayout().setBackground(getContext().getDrawable(R.drawable.btn_white_bottom_right_corner));
        }
        mnemonicTextView.getBtnDelete().setVisibility(View.INVISIBLE);

        MnemonicItem originItem = mOriginList.get(position);
        if(!TextUtils.isEmpty(memonicItem.getWord()) && !originItem.getWord().equals(memonicItem.getWord())){
            mnemonicTextView.getBtnDelete().setVisibility(View.VISIBLE);
            mnemonicTextView.getTextWordView().setTextColor(ContextCompat.getColor(getContext(),R.color.alarm_highlight_text_color));
        }else{
            mnemonicTextView.getBtnDelete().setVisibility(View.INVISIBLE);
            mnemonicTextView.getTextWordView().setTextColor(ContextCompat.getColor(getContext(),R.color.global_main_text_color));
        }
        mnemonicTextView.getTextWordView().setText(memonicItem.getWord());
        mnemonicTextView.getTextWordIndexView().setText(memonicItem.getIndex()+"");
    }
}
