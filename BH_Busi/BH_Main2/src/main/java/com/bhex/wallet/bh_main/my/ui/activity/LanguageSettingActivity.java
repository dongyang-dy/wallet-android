package com.bhex.wallet.bh_main.my.ui.activity;


import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.lib.uikit.widget.RecycleViewExtDivider;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.tools.language.LocalManageUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.LanguageAdapter;
import com.bhex.wallet.bh_main.my.model.LanguageItem;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.event.LanguageEvent;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.utils.LanguageConstants;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;

@Route(path= ARouterConfig.My.My_Languae_Set)
public class LanguageSettingActivity extends BaseActivity {

    @Autowired(name= BHConstants.TITLE)
    String title;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.rcv_language_set)
    RecyclerView rcvLanguageSet;

    LanguageAdapter mLanguageAdapter;
    
    private List<LanguageItem> mLanguageList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_language_setting;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(title);

        initData();

        mLanguageAdapter = new LanguageAdapter(R.layout.item_language,mLanguageList);
        rcvLanguageSet.setAdapter(mLanguageAdapter);

        RecycleViewExtDivider divider = new RecycleViewExtDivider(
                this, LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(this,32),0,
                getResources().getColor(R.color.global_divider_color));

        rcvLanguageSet.addItemDecoration(divider);
    }

    /**
     * 初始化语言数据
     */
    private void initData() {
        Locale locale = LocalManageUtil.getSetLanguageLocale(this);

        int  selectIndex = LocalManageUtil.getSetLanguageLocaleIndex(this);

        mLanguageList = new ArrayList<>();
        String []langArray = getResources().getStringArray(R.array.app_language_type);

        for (int i = 0; i < langArray.length; i++) {
            LanguageItem language = new LanguageItem();
            language.setFullName(langArray[i]);
            if((i+1)==selectIndex){
                language.setSelected(true);
            }else{
                language.setSelected(false);
            }

            language.setId(i+1);
            language.setShortName(LanguageConstants.SHORT_NAMES[i]);
            mLanguageList.add(language);
        }
    }

    @Override
    protected void addEvent() {
        mLanguageAdapter.setOnItemClickListener((adapter, view, position) -> {
            changeLanguage(position);
        });
    }

    /**
     * 语言设置
     * @param position
     */
    private void changeLanguage(int position){
        Observable.just(Integer.valueOf(position))
                //.compose(RxSchedulersHelper.io_main())
                .map(integer -> {
                    LanguageItem languageEntity = mLanguageList.get(integer);
                    LocalManageUtil.saveSelectLanguage(LanguageSettingActivity.this, languageEntity.getId());
                    EventBus.getDefault().post(new LanguageEvent());
                    return Boolean.valueOf(true);
                })
                .delay(1200L, TimeUnit.MILLISECONDS)
                .subscribe(
                        new BHProgressObserver<Boolean>(LanguageSettingActivity.this,
                                getResources().getString(R.string.langeuage_setting)){
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            finish();
                        }
        });



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
