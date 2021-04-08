package com.bhex.wallet.mnemonic.adapter;

import androidx.core.content.ContextCompat;

import com.bhex.lib.uikit.widget.MnemonicTextView;
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
public class MnemonicAdapter extends BaseQuickAdapter<MnemonicItem, BaseViewHolder> {

    public MnemonicAdapter( @Nullable List<MnemonicItem> data) {
        super(R.layout.item_mnemonic, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable MnemonicItem memonicItem) {
        //MnemonicTextView mnemonicTextView =  baseViewHolder.getView(R2.id.mtv_word);

        MnemonicTextView mnemonicTextView =  viewHolder.getView(R.id.mtv_word);
        mnemonicTextView.getTextWordView().setText(memonicItem.getWord());
        mnemonicTextView.getTextWordIndexView().setText(memonicItem.getIndex()+"");

        if(memonicItem.isSelected()){
            mnemonicTextView.getTextWordView().setTextColor(ContextCompat.getColor(getContext(),R.color.mnemonic_filled_text));
        }else{
            mnemonicTextView.getTextWordView().setTextColor(ContextCompat.getColor(getContext(),R.color.global_main_text_color));
        }
    }
}
