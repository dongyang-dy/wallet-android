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
import com.bhex.wallet.bh_main.R;
import com.bhex.wallet.bh_main.R2;
import com.bhex.wallet.bh_main.my.adapter.SecuritySettingAdapter;
import com.bhex.wallet.bh_main.my.helper.MyHelper;
import com.bhex.wallet.bh_main.my.model.SecuritySettingItem;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.manager.SecuritySettingManager;
import com.bhex.wallet.common.ui.fragment.Password30PFragment;

import java.util.List;

import butterknife.BindView;

/**
 * @author gongdongyang
 * 2020-10-15 20:27:22
 * 安全设置
 */
@Route(path = ARouterConfig.My.My_Security_Setting, name = "安全设置")
public class SecuritySettingActivity extends BaseActivity implements Password30PFragment.PasswordClickListener {


    @BindView(R2.id.rcy_choose_list)
    RecyclerView rcy_choose_list;
    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;

    private SecuritySettingAdapter mSecSetAdapter;
    private List<SecuritySettingItem> mItems;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_security_setting;
    }

    @Override
    protected void initView() {
        tv_center_title.setText(getString(R.string.safe_setting));
        
        mItems = MyHelper.getSecSettingItems(this);
        mSecSetAdapter = new SecuritySettingAdapter(mItems,null);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rcy_choose_list.setAdapter(mSecSetAdapter);
        rcy_choose_list.setLayoutManager(llm);

        RecycleViewExtDivider ItemDecoration = new RecycleViewExtDivider(
                this, LinearLayoutManager.VERTICAL,
                PixelUtils.dp2px(this, 16), 0,
                ColorUtil.getColor(this, R.color.global_divider_color));

        rcy_choose_list.addItemDecoration(ItemDecoration);
    }

    @Override
    protected void addEvent() {
        mSecSetAdapter.setOnItemClickListener((adapter, view, position) -> {
            updateItemStatus(position);
        });
    }

    //设置
    private void updateItemStatus(int position){
        SecuritySettingItem item = mItems.get(position);
        if(item.isSelected){
            return;
        }

        if(position==0){
            //每次都输入密码
            updateViewStatus(position,"");
        }else if(position==1){
            //免密码输入
            Password30PFragment.showPasswordDialog(getSupportFragmentManager(),Password30PFragment.class.getName(),this,position,false);
        }
    }

    @Override
    public void confirmAction(String password, int position, int way) {
        updateViewStatus(position,password);
    }

    private void updateViewStatus(int position,String pwd){
        for(int i=0;i<mItems.size();i++){
            if(i==position){
                mItems.get(i).isSelected = true;
            }else{
                mItems.get(i).isSelected = false;
            }
        }
        mSecSetAdapter.notifyDataSetChanged();

        switch (position){
            case 0:
                SecuritySettingManager.getInstance().request_thirty_in_time(false,"");
                break;
            case 1:
                SecuritySettingManager.getInstance().request_thirty_in_time(true,pwd);
                break;
            default:
                break;
        }
    }
}