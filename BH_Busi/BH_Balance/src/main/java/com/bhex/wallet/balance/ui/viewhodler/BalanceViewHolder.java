package com.bhex.wallet.balance.ui.viewhodler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.lib.uikit.widget.layout.XUILinearLayout;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SequenceManager;


public class BalanceViewHolder {
    public View viewHolder;
    public Context mContext;
    public AppCompatTextView tv_wallet_name;
    public AppCompatImageView iv_eye;
    public AppCompatTextView tv_asset;
    public XUILinearLayout layout_operator_action;

    //public CardView layout_index_1;

    public BHWallet bhWallet;

    public FrameLayout layout_test_label;

    public BalanceViewHolder(View viewHolder, Context mContext) {
        this.viewHolder = viewHolder;
        this.mContext = mContext;

        tv_wallet_name = viewHolder.findViewById(R.id.tv_wallet_name);
        iv_eye = viewHolder.findViewById(R.id.iv_eye);
        tv_asset = viewHolder.findViewById(R.id.tv_asset);

        layout_test_label = viewHolder.findViewById(R.id.layout_test_label);

        //设置背景
        GradientDrawable drawable = ShapeUtils.getRoundRectBottomDrawable(PixelUtils.dp2px(mContext,6), Color.parseColor("#190A1825"));
        layout_test_label.setBackground(drawable);

        layout_operator_action = viewHolder.findViewById(R.id.layout_operator_action);
        layout_operator_action.setShadowColor(ColorUtil.getColor(mContext,R.color.highlight_text_color));
    }

    public void initContent(){
        //领取测试币背景色设置
        FrameLayout btn_claim_test_coin = viewHolder.findViewById(R.id.btn_claim_test_coin);
        GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mContext,16), ColorUtil.getColor(mContext,R.color.white));
        btn_claim_test_coin.setBackground(drawable);

        //当前钱包账户
        bhWallet = BHUserManager.getInstance().getCurrentBhWallet();

        viewHolder.findViewById(R.id.btn_transfer_in).setOnClickListener(this::btnClickAction);
        viewHolder.findViewById(R.id.btn_transfer_out).setOnClickListener(this::btnClickAction);
        viewHolder.findViewById(R.id.btn_entrust).setOnClickListener(this::btnClickAction);
        //设置姓名
        tv_wallet_name.setText(bhWallet.name);
        //添加账户
        viewHolder.findViewById(R.id.iv_create_wallet).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.Trusteeship.Trusteeship_Add_Index).withInt("flag",1).navigation();
        });

        //balanceViewHolder
        AppCompatImageView iv_announce_type = viewHolder.findViewById(R.id.iv_announce_type);

        Drawable announce_type_drawable = ColorUtil.getDrawable(mContext,R.mipmap.ic_announcement,R.color.highlight_text_color);
        iv_announce_type.setImageDrawable(announce_type_drawable);
    }

    //
    private void btnClickAction(View view) {
        if(view.getId()==R.id.btn_transfer_in){
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_in)
                    .withString("symbol", BHConstants.BHT_TOKEN)
                    .withInt("way", BH_BUSI_TYPE.链内转账.getIntValue())
                    .navigation();
        }else if(view.getId()==R.id.btn_transfer_out){
            ARouter.getInstance().build(ARouterConfig.Balance.Balance_transfer_out)
                    .withString("symbol", BHConstants.BHT_TOKEN)
                    .withInt("way", BH_BUSI_TYPE.链内转账.getIntValue())
                    .navigation();
        }else if(view.getId()==R.id.btn_entrust){
            ARouter.getInstance().build(ARouterConfig.Validator.Validator_Index)
                    .navigation();
        }
    }



    /*//计算币种法币值
    public double calculateAllTokenPrice(Context context, AccountInfo accountInfo, List<BHChain> mOriginBalanceList){
        double allTokenPrice = 0;

        List<AccountInfo.AssetsBean> list = accountInfo.getAssets();
        if(list==null || list.size()==0){
            return allTokenPrice;
        }
        Map<String,AccountInfo.AssetsBean> map = new HashMap<>();
        for(AccountInfo.AssetsBean bean:list){
            map.put(bean.getSymbol(),bean);
            //计算每一个币种的资产价值

            double amount = TextUtils.isEmpty(bean.getAmount())?0:Double.valueOf(bean.getAmount());

            //法币价值
            double symbolPrice = CurrencyManager.getInstance().getCurrencyRate(context,bean.getSymbol());

            //LogUtils.d("BalanceFragment==>:","amount==="+bean.getAmount()+"=="+symbolPrice);
            double asset = NumberUtil.mul(String.valueOf(amount),String.valueOf(symbolPrice));
            allTokenPrice = NumberUtil.add(asset,allTokenPrice);

        }
        return allTokenPrice;
    }*/
}
