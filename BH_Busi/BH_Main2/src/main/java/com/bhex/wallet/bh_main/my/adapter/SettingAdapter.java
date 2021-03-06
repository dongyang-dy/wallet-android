package com.bhex.wallet.bh_main.my.adapter;

import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.SwitchCompat;

import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.manager.MMKVManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/12
 * Time: 11:19
 */
public class SettingAdapter extends BaseQuickAdapter<MyItem, BaseViewHolder> {

    //private SwitchCheckListener switchCheckListener;

    public SettingAdapter( @Nullable List<MyItem> data) {
        super(R.layout.item_setting, data);
        //addChildClickViewIds(R.id.sc_theme);
        addChildClickViewIds(R.id.ck_select);

    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable MyItem myItem) {
        viewHolder.setText(R.id.tv_title,myItem.title);

        if(myItem.isArrow){
            viewHolder.getView(R.id.iv_arrow).setVisibility(View.VISIBLE);
            viewHolder.getView(R.id.tv_right_txt).setVisibility(View.INVISIBLE);
        }else{
            viewHolder.getView(R.id.iv_arrow).setVisibility(View.INVISIBLE);
            viewHolder.getView(R.id.tv_right_txt).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_right_txt,myItem.rightTxt);
        }
        viewHolder.setText(R.id.tv_right_2_txt,myItem.rightTxt);
        SwitchCompat sc = viewHolder.getView(R.id.sc_theme);
        AppCompatCheckedTextView ck = viewHolder.getView(R.id.ck_select);
        ck.setVisibility(View.GONE);

        int position = getItemPosition(myItem);
        if(position==2){
            int model = MMKVManager.getInstance().getSelectNightMode();
            if(model== AppCompatDelegate.MODE_NIGHT_YES){
                ck.setChecked(true);
            }else{
                ck.setChecked(false);
            }
            viewHolder.getView(R.id.iv_arrow).setVisibility(View.INVISIBLE);
            sc.setVisibility(View.GONE);
            ck.setVisibility(View.VISIBLE);
        }else{
            sc.setVisibility(View.INVISIBLE);
            viewHolder.getView(R.id.iv_arrow).setVisibility(View.VISIBLE);
        }

        if(position==4){
            boolean isFinger = MMKVManager.getInstance().mmkv().decodeBool(BHConstants.FINGER_PWD_KEY);
            if(isFinger){
                ck.setChecked(true);
            }
            sc.setVisibility(View.GONE);
            ck.setVisibility(View.VISIBLE);
        }

    }


}
