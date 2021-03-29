package com.bhex.wallet.bh_main.my.ui.activity;


import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.RateAdapter;
import com.bhex.wallet.bh_main.my.model.CurrencyItem;
import com.bhex.wallet.bh_main.my.presenter.RatePresenter;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.CURRENCY_TYPE;
import com.bhex.wallet.common.event.CurrencyEvent;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MMKVManager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;

/**
 * @author gongdongyang
 * 2020-4-18 16:13:56
 * 汇率设置
 */
@Route(path = ARouterConfig.My.My_Rate_setting)
public class RateSettingActivity extends BaseActivity<RatePresenter> {

    @Autowired(name="title")
    String title;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.recycler_rate)
    RecyclerView recycler_rate;

    //String []currencyArray ;

    List<CurrencyItem> currencyItemList;

    RateAdapter mRateAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_rate_setting;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new RatePresenter(this);
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        CURRENCY_TYPE.initCurrency(this);

        //currencyArray = getResources().getStringArray(R.array.Currency_list);

        currencyItemList = mPresenter.getAllCurrency(this);
        tv_center_title.setText(title);
        mRateAdapter = new RateAdapter(currencyItemList);
        recycler_rate.setAdapter(mRateAdapter);
        RecycleViewExtDivider ItemDecoration = new RecycleViewExtDivider(
                this,LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(this,32),0,
                ColorUtil.getColor(this,R.color.global_divider_color));
        recycler_rate.addItemDecoration(ItemDecoration);
    }

    @Override
    protected void addEvent() {
        mRateAdapter.setOnItemClickListener((adapter, view, position) -> {
            int oldPosition = mPresenter.getOldPosition(currencyItemList);
            if(oldPosition==position){
                return;
            }

            CurrencyItem oldItem = currencyItemList.get(oldPosition);
            oldItem.selected = !oldItem.selected;
            mRateAdapter.notifyItemChanged(oldPosition);

            CurrencyItem currencyItem = currencyItemList.get(position);
            currencyItem.selected = !currencyItem.selected;
            //mRateAdapter.notifyDataSetChanged();

            mRateAdapter.notifyItemChanged(position);

            changeCurrency(position);
        });
    }

    /**
     * 选择当前货币
     * @param position
     */
    private void changeCurrency(int position) {

        Observable.just(Integer.valueOf(position))
                .map(integer -> {
                    CurrencyItem currencyItem = currencyItemList.get(position);
                    CurrencyManager.getInstance().setCurrency(currencyItem.shortName);
                    MMKVManager.getInstance().mmkv().encode(BHConstants.CURRENCY_USED,currencyItem.shortName);
                    EventBus.getDefault().post(new CurrencyEvent());
                    return Boolean.valueOf(true);
                })
                .delay(300L, TimeUnit.MILLISECONDS)
                .subscribe(new BHBaseObserver<Boolean>() {
                    @Override
                    protected void onSuccess(Boolean aBoolean) {
                        finish();
                    }
                });

    }

}
