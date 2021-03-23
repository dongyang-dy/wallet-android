package com.bhex.wallet.mnemonic.ui.activity;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.ColorUtil;
import com.bhex.wallet.common.base.BaseCacheActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.enums.MAKE_WALLET_TYPE;
import com.bhex.wallet.common.manager.MainActivityManager;
import com.bhex.wallet.common.utils.ARouterUtil;
import com.bhex.wallet.mnemonic.R;
import com.bhex.wallet.mnemonic.R2;
import com.bhex.wallet.mnemonic.persenter.ImportPresenter;
import com.bhex.wallet.mnemonic.ui.AddWalletDecoration;
import com.bhex.wallet.mnemonic.ui.item.FunctionItem;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;

/**
 * @author gdy
 * 2020-12-2 10:47:43
 */
@Route(
        path = ARouterConfig.Trusteeship.Trusteeship_Add_Index,
        name = "添加钱包"
)
public class AddWalletActivity extends BaseCacheActivity<ImportPresenter> {

    @Autowired (name= BHConstants.FLAG)
    int flag = 0;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_wallet;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        String title = (flag==0)?getResources().getString(R.string.wallet_import_trusteeship)
                :getResources().getString(R.string.add_wallet);

        tv_center_title.setText(title);

        //只有导入功能
        if(flag==0){
            findViewById(R.id.layout_create).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void addEvent() {
        //导入助记词
        findViewById(R.id.layout_import_mnemonic).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_IMPORT_MNEMONIC)
                    .navigation();
        });
        //导入KS
        findViewById(R.id.layout_import_keystore).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_IMPORT_KEYSTORE)
                    .navigation();
        });

        //导入PK
        findViewById(R.id.layout_import_pk).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_IMPORT_PRIVATEKEY)
                    .navigation();
        });
        //创建助记词
        findViewById(R.id.layout_create_wallet).setOnClickListener(v->{
            ARouter.getInstance().build(ARouterConfig.TRUSTEESHIP_MNEMONIC_FRIST)
                    .navigation();
        });
    }
}