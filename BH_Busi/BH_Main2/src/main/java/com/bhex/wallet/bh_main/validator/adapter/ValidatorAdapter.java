package com.bhex.wallet.bh_main.validator.adapter;

import android.text.TextUtils;

import androidx.appcompat.widget.AppCompatTextView;

import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.validator.model.ValidatorInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by BHEX.
 * User: zhouchang
 * Date: 2020/4/14
 */
public class ValidatorAdapter extends BaseQuickAdapter<ValidatorInfo, BaseViewHolder> {

    private int mValid;
    public ValidatorAdapter(int isValid,@Nullable List<ValidatorInfo> data) {
        super( R.layout.item_validator,data);
        mValid = isValid;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable ValidatorInfo validatorInfo) {
        viewHolder.setText(R.id.tv_validator_name,validatorInfo.getDescription().getMoniker());
        if (mValid == BHConstants.VALIDATOR_VALID) {
            viewHolder.setTextColor(R.id.tv_validator_name, getContext().getResources().getColor(R.color.global_main_text_color));
            viewHolder.setImageResource(R.id.iv_status, R.mipmap.icon_validator_valid);
        } else {
            viewHolder.setTextColor(R.id.tv_validator_name, getContext().getResources().getColor(R.color.global_secondary_text_color));
            viewHolder.setImageResource(R.id.iv_status, R.mipmap.icon_validator_invalid);
        }


        String v_voting_power_proportion = "";
        if(!TextUtils.isEmpty(validatorInfo.getVoting_power_proportion())){
            v_voting_power_proportion = validatorInfo.getVoting_power_proportion()+"%";
        }
        viewHolder.setText(R.id.tv_voting_power_proportion, v_voting_power_proportion);

        String v_elf_delegate_proportion = "";
        if(!TextUtils.isEmpty(validatorInfo.getSelf_delegate_proportion())){
            v_elf_delegate_proportion = validatorInfo.getSelf_delegate_proportion();
        }
        viewHolder.setText(R.id.tv_self_delegate_proportion,v_elf_delegate_proportion);

        String v_apy = "";
        if(!TextUtils.isEmpty(validatorInfo.apy)){
            v_apy = validatorInfo.apy+"%";
        }
        viewHolder.setText(R.id.tv_other_delegate_proportion,v_apy);

    }

}
