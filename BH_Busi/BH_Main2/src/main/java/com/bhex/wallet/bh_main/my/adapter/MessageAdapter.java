package com.bhex.wallet.bh_main.my.adapter;

import com.bhex.tools.utils.DateUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.my.model.BHMessage;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.MMKVManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 * 2020-5-21 16:56:11
 * 消息
 */
public class MessageAdapter extends BaseQuickAdapter<BHMessage, BaseViewHolder> {
    private String mKey = "";
    public MessageAdapter( @Nullable List<BHMessage> data,String msgType) {
        super(R.layout.item_message, data);
        String address = BHUserManager.getInstance().getCurrentBhWallet().address;
        mKey = address+"_"+msgType+"_"+ BH_BUSI_TYPE.已读消息.value;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder vh, BHMessage bhm) {
        try{
            String []message_type = getContext().getResources().getStringArray(R.array.Message_type);

            vh.setText(R.id.tv_tx_type,message_type[bhm.tx_type-1]);
            //token-name

            vh.setText(R.id.tv_tx_amount,bhm.amount+" "+bhm.name.toUpperCase());
            vh.setText(R.id.tv_tx_status, R.string.success);

            //时间格式化
            String tv_time = DateUtil.transTimeWithPattern(bhm.time*1000,DateUtil.DATA_TIME_STYLE);
            vh.setText(R.id.tv_tx_time,tv_time);

            String v_read_msg_id = MMKVManager.getInstance().mmkv().decodeString(mKey,"");
            if(v_read_msg_id.contains(bhm.id+"")){
                bhm.read = true;
            }else{
                bhm.read = false;
            }
            if(bhm.read){
                vh.setTextColorRes(R.id.tv_tx_type, R.color.global_label_text_color);
                vh.setTextColorRes(R.id.tv_tx_amount, R.color.global_label_text_color);
            }else{
                vh.setTextColorRes(R.id.tv_tx_type, R.color.global_main_text_color);
                vh.setTextColorRes(R.id.tv_tx_amount, R.color.global_main_text_color);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
