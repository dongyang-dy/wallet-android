package com.bhex.wallet.balance.adapter;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.bhex.lib.uikit.util.ShapeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.ImageLoaderUtil;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHBalanceHelper;
import com.bhex.wallet.balance.model.BHTokenItem;
import com.bhex.wallet.balance.ui.activity.ChainTokenActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.enums.CURRENCY_TYPE;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.model.AccountInfo;
import com.bhex.wallet.common.model.BHBalance;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.bhex.wallet.common.viewmodel.BalanceViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/18
 * Time: 0:18
 */
public class BalanceAdapter extends BaseQuickAdapter<BHTokenItem, BaseViewHolder> {

    private String isHidden = "0";
    private BalanceViewModel mBalanceViewModel;
    private ChainTokenActivity mActivity;
    private LinkedHashMap<String,BaseViewHolder> mItemViews = new LinkedHashMap<>();

    public BalanceAdapter( ChainTokenActivity activity,@Nullable List<BHTokenItem> data) {
        super(R.layout.item_balance, data);
        mActivity = activity;
        mBalanceViewModel = ViewModelProviders.of(MainActivityManager._instance.mainActivity).get(BalanceViewModel.class).build(mActivity);
        //资产订阅
        LiveDataBus.getInstance().with(BHConstants.Label_Account, LoadDataModel.class).observe(activity, ldm->{
            if(ldm.loadingStatus== LoadingStatus.SUCCESS){
                updateAssets((AccountInfo) ldm.getData());
            }
        });
    }


    @Override
    public void onBindViewHolder(@NotNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        BHToken balanceItem = getData().get(position);
        if(mItemViews.get(balanceItem.symbol)==null){
            mItemViews.put(balanceItem.symbol,holder);
        }
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, @Nullable BHTokenItem bhTokenItem) {
        AppCompatImageView iv = viewHolder.getView(R.id.iv_coin);
        iv.setImageResource(0);
        ImageLoaderUtil.loadImageView(getContext(),
                bhTokenItem.logo, iv,R.mipmap.ic_default_coin);

        viewHolder.setText(R.id.tv_coin_name,bhTokenItem.name.toUpperCase());
        BHBalance balanceItem = BHBalanceHelper.getBHBalanceFromAccount(bhTokenItem.symbol);

        //更新资产和数量
        updatePriceAndAmount(balanceItem,viewHolder);
        //标签
        AppCompatTextView tv_coin_type = viewHolder.getView(R.id.tv_coin_type);
        BHToken bhCoin  = CacheCenter.getInstance().getSymbolCache().getBHToken(balanceItem.symbol);

        if(bhCoin==null){
            return;
        }
        if(bhCoin.name.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            tv_coin_type.setVisibility(View.GONE);
            tv_coin_type.setBackgroundColor(0);
        }else if(bhCoin.is_verified){
            tv_coin_type.setVisibility(View.VISIBLE);
            tv_coin_type.setText(getContext().getString(R.string.verified));
            tv_coin_type.setTextColor(ColorUtil.getColor(mActivity,R.color.verify_token_text_color));
            GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mActivity,10), ColorUtil.getColor(mActivity,R.color.verify_token_bg_color));
            tv_coin_type.setBackgroundDrawable(drawable);
        } else if(bhCoin.is_native){
            tv_coin_type.setVisibility(View.VISIBLE);
            tv_coin_type.setText(getContext().getString(R.string.native_token));
            tv_coin_type.setTextColor(ColorUtil.getColor(mActivity,R.color.native_token_text_color));
            GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mActivity,10), ColorUtil.getColor(mActivity,R.color.native_token_bg_color));
            tv_coin_type.setBackgroundDrawable(drawable);

        }
        else {
            tv_coin_type.setVisibility(View.VISIBLE);
            tv_coin_type.setText(getContext().getString(R.string.no_native_token));
            tv_coin_type.setTextColor(ColorUtil.getColor(mActivity,R.color.cross_token_text_color));
            GradientDrawable drawable = ShapeUtils.getRoundRectDrawable(PixelUtils.dp2px(mActivity,10), ColorUtil.getColor(mActivity,R.color.cross_token_bg_color));
            tv_coin_type.setBackgroundDrawable(drawable);
        }

    }

    public void setIsHidden(String isHidden) {
        this.isHidden = isHidden;
        notifyDataSetChanged();
    }

    private void updateAssets(AccountInfo accountInfo) {
        List<AccountInfo.AssetsBean> list = accountInfo.assets;
        if(list==null || list.size()==0){
            return ;
        }

        for(AccountInfo.AssetsBean bean:list){
            //获取bean在recycleView中的位置
            int position = getPosition(bean);
            if(position==-1){
                continue;
            }

            BaseViewHolder itemView = mItemViews.get(getData().get(position).symbol);
            //

            BHToken bhToken = getData().get(position);
            BHBalance balanceItem = BHBalanceHelper.getBHBalanceFromAccount(bhToken.symbol);

            //实时价格、资产和数量
            updatePriceAndAmount(balanceItem,itemView);
        }
    }

    private int getPosition(AccountInfo.AssetsBean bean){
        int position = -1;
        List<BHTokenItem> list = getData();
        for(int i=0;i<list.size();i++){
            BHTokenItem item = list.get(i);
            if(!item.symbol.equalsIgnoreCase(bean.symbol)){
                continue;
            }
            position = item.index;
            //item.amount = bean.getAmount();
            return position;
        }
        return position;
    }

    //更新资产和数量
    private void updatePriceAndAmount(BHBalance balanceItem,BaseViewHolder viewHolder){

        if(viewHolder==null){
            return;
        }

        //实时价格
        String symbol_prices = CurrencyManager.getInstance().getCurrencyRateDecription(getContext(),balanceItem.symbol);
        viewHolder.setText(R.id.tv_coin_price, symbol_prices);
        //币的数量
        if(isHidden.equals("0")){
            if(!TextUtils.isEmpty(balanceItem.amount) && Double.valueOf(balanceItem.amount)>0) {
                String []result = BHBalanceHelper.getAmountToCurrencyValue(getContext(),balanceItem.amount,balanceItem.symbol,false);
                viewHolder.setText(R.id.tv_coin_amount, result[0]);
                viewHolder.setText(R.id.tv_coin_count, "≈"+result[1]);
            }else{
                viewHolder.setText(R.id.tv_coin_amount, "0");
                viewHolder.setText(R.id.tv_coin_count, "≈"+
                        CURRENCY_TYPE.valueOf(CurrencyManager.getInstance().loadCurrency(getContext()).toUpperCase()).character+"0");
            }
        }else{
            viewHolder.setText(R.id.tv_coin_amount, "***");
            viewHolder.setText(R.id.tv_coin_count, "***");
        }
    }

    public void clear(){
        getData().clear();
        mItemViews.clear();
    }

}
