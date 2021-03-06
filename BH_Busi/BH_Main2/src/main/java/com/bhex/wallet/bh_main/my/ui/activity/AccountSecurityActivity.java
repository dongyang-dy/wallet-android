package com.bhex.wallet.bh_main.my.ui.activity;

import android.view.View;
import android.widget.CheckedTextView;

import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.MMKVManager;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.utils.SafeUilts;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2021-3-5 16:08:06
 * 账户与安全
 */
@Route(path = ARouterConfig.My.My_Account_Security,name="账户与安全")
public class AccountSecurityActivity extends BaseActivity {

    @BindView(R2.id.rcv_function)
    RecyclerView rcv_function;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    private AccountSecurityAdapter adapter;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_security;
    }

    @Override
    protected void initView() {
        tv_center_title.setText(getString(R.string.account_securtiy));
        List<MyItem> list = MyHelper.getAccountSecurityList(this);
        rcv_function.setAdapter(adapter=new AccountSecurityAdapter(list));
    }

    @Override
    protected void addEvent() {
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            switch (position){
                case 0:
                    ARouter.getInstance().build(ARouterConfig.ACCOUNT_MANAGER_PAGE).navigation();
                    break;
                case 1:
                    ARouter.getInstance().build(ARouterConfig.My.My_Security_Setting).navigation();
                    break;
                case 2:
                    break;
            }
        });

        //
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            CheckedTextView ck = (CheckedTextView) view;
            if(!ck.isChecked()){
                if(SafeUilts.isFinger(this)){
                    MMKVManager.getInstance().mmkv().encode(BHConstants.FINGER_PWD_KEY,true);
                    ck.toggle();
                    ToastUtils.showToast(getResources().getString(R.string.set_finger_ok));
                }
            }else {
                MMKVManager.getInstance().mmkv().remove(BHConstants.FINGER_PWD_KEY);
                ck.toggle();
            }
        });
    }

    class  AccountSecurityAdapter extends BaseQuickAdapter<MyItem, BaseViewHolder>{

        public AccountSecurityAdapter( @Nullable List<MyItem> data) {
            super(R.layout.item_setting, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, MyItem myItem) {
            holder.setText(R.id.tv_title,myItem.title);
            holder.setVisible(R.id.ck_select,false);
            holder.setText(R.id.tv_right_2_txt,"");
            //安全设置
            if(myItem.title.equals(getString(R.string.safe_setting))){
                if(SecuritySettingManager.getInstance().thirty_in_time){
                    holder.setText(R.id.tv_right_2_txt,getString(R.string.thirty_minute_noneed_paaword));
                }else{
                    holder.setText(R.id.tv_right_2_txt,getString(R.string.everytime_need_password));
                }
            }
            SwitchCompat sc = holder.getView(R.id.sc_theme);
            AppCompatCheckedTextView ck = holder.getView(R.id.ck_select);
            ck.setVisibility(View.GONE);

            int position = getItemPosition(myItem);
            if(position==2){
                boolean isFinger = MMKVManager.getInstance().mmkv().decodeBool(BHConstants.FINGER_PWD_KEY);
                if(isFinger){
                    ck.setChecked(true);
                }
                sc.setVisibility(View.GONE);
                ck.setVisibility(View.VISIBLE);
            }

        }
    }

}