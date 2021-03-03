package com.bhex.wallet.bh_main.my.ui.activity;

/**
 * @au
 */
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.AccountFunctionAdapter;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.ui.decoration.FunctionItemDecoration;
import com.bhex.wallet.bh_main.my.ui.fragment.UpdateName2Fragment;
import com.bhex.wallet.bh_main.my.ui.item.MyItem;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.manager.BHUserManager;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 账户详情
 * 2021-3-2 22:48:39
 */
@Route(path = ARouterConfig.My.ACCOUNT_DETAIL_PAGE, name = "账户详情")
public class AccountDetailActivity extends BaseActivity {

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @BindView(R2.id.rcv_account_function)
    RecyclerView rcv_account_function;

    AccountFunctionAdapter accountAdapter;

    BHWallet bhWallet;

    @Autowired(name="wallet_address")
    String wallet_address;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_detail;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        tv_center_title.setText(getString(R.string.account_detail));
        bhWallet = getBHWalletId();
        List<MyItem> list = MyHelper.getAcountDetailFunction(this);
        rcv_account_function.setAdapter(accountAdapter = new AccountFunctionAdapter(list,bhWallet.name));

        FunctionItemDecoration itemDecoration = new FunctionItemDecoration(this,
                ColorUtil.getColor(this,R.color.global_divider_color),
                getResources().getDimension(R.dimen.item_line_divider_height));
        rcv_account_function.addItemDecoration(itemDecoration);
    }

    @Override
    protected void addEvent() {
        accountAdapter.setOnItemClickListener((adapter, view, position) -> {
            if(position==0){
                UpdateName2Fragment.getInstance(bhWallet,this::updateNameCallback).show(AccountDetailActivity.this.getSupportFragmentManager(),
                        UpdateName2Fragment.class.getName());
            }else{

            }

        });
    }

    //修改用户名称-回调
    private void updateNameCallback() {
        bhWallet = getBHWalletId();
        accountAdapter.setWalletName(bhWallet.name);
        accountAdapter.notifyDataSetChanged();
    }

    // 通过Id 获取Wallet
    private  BHWallet getBHWalletId( ){
        List<BHWallet> list = BHUserManager.getInstance().getAllWallet();
        for(BHWallet item:list){
            if(wallet_address.equals(item.address)){
                return item;
            }
        }
        return null;
    }


}