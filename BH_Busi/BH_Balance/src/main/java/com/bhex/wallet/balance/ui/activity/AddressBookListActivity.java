package com.bhex.wallet.balance.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib.uikit.widget.EmptyLayout;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.PixelUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.R2;
import com.bhex.wallet.balance.viewmodel.AddressBookViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHAddressBook;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHToken;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.button.MaterialButton;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author gongdongyang
 */
@Route(path = ARouterConfig.Balance.Balance_address_list, name = "地址列表")
public class AddressBookListActivity extends BaseActivity {
    //public static final int result_address = 100;
    public static final int REQUEST_ADDRESS = 101;
    public static final String RESULT_DATA = "result_data";

    @Autowired(name = BHConstants.SYMBOL)
    public String symbol;

    @Autowired(name = "address")
    public String address;

    private BHToken bhToken;

    @BindView(R2.id.tv_center_title)
    AppCompatTextView tv_center_title;
    @BindView(R2.id.rec_address_list)
    SwipeRecyclerView rec_address_list;
    @BindView(R2.id.empty_layout)
    EmptyLayout empty_layout;


    private AddressBookViewModel addressBookViewModel;
    private AddressBookListAdapter bookListAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_address_book_list;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        bhToken = SymbolCache.getInstance().getBHToken(symbol);
        //设置标题
        tv_center_title.setText(getString(R.string.address_book));
        findViewById(R.id.btn_address_add).setOnClickListener(v -> {
            ARouter.getInstance()
                    .build(ARouterConfig.Balance.Balance_address_add)
                    .withString(BHConstants.SYMBOL,symbol)
                    .navigation();
        });
        rec_address_list.setSwipeMenuCreator(swipeMenuCreator);
        rec_address_list.setOnItemMenuClickListener(mMenuItemClickListener);
        rec_address_list.setAdapter(bookListAdapter = new AddressBookListAdapter(null,address));
        empty_layout.showProgess();
    }

    @Override
    protected void addEvent() {
        addressBookViewModel = ViewModelProviders.of(this).get(AddressBookViewModel.class);
        addressBookViewModel.mutableLiveData.observe(this, ldm -> {
            updateAddressList(ldm);
        });
        addressBookViewModel.deleteLiveData.observe(this,ldm->{
            deleteAddressStatus(ldm);
        });

        bookListAdapter.setOnItemClickListener((adapter, view, position) -> {
            BHAddressBook addressBook = bookListAdapter.getData().get(position);
            Intent intent =  getIntent();
            intent.putExtra(RESULT_DATA,addressBook.address);
            AddressBookListActivity.this.setResult(RESULT_OK,intent);
            AddressBookListActivity.this.finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String current_wallet_address = BHUserManager.getInstance().getCurrentBhWallet().address;
        addressBookViewModel.loadAddressBookList(this,current_wallet_address,bhToken.chain);
    }

    //更新列表数据
    private void updateAddressList(LoadDataModel<List<BHAddressBook>> ldm) {
        if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
            List<BHAddressBook> list =  ldm.getData();
            bookListAdapter.getData().clear();
            bookListAdapter.addData(list);
            if(!ToolUtils.checkListIsEmpty(bookListAdapter.getData())){
                empty_layout.loadSuccess();
            }else{
                empty_layout.showNoData();
            }
        }else{

        }
    }

    private void deleteAddressStatus(LoadDataModel ldm){
        if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
            String current_wallet_address = BHUserManager.getInstance().getCurrentBhWallet().address;
            addressBookViewModel.loadAddressBookList(this,current_wallet_address,bhToken.chain);
        }
    }

    class AddressBookListAdapter extends BaseQuickAdapter<BHAddressBook, BaseViewHolder> {
        public AddressBookListAdapter(@Nullable List<BHAddressBook> data,String address) {
            super(R.layout.item_address_book, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, BHAddressBook addressBook) {
            holder.setText(R.id.tv_address_name,addressBook.address_name);
            holder.setText(R.id.tv_address,addressBook.address);

            if(TextUtils.isEmpty(addressBook.address_remark)){
                holder.setGone(R.id.tv_address_remark,true);
            }else{
                holder.setVisible(R.id.tv_address_remark,true);
                holder.setText(R.id.tv_address_remark,addressBook.address_remark);
            }


            AppCompatCheckBox ck_address_select = holder.getView(R.id.ck_address_select);
            if(!address.equals(addressBook.address)){
                ck_address_select.setChecked(false);
                ck_address_select.setVisibility(View.GONE);
            }else{
                ck_address_select.setChecked(true);
                ck_address_select.setVisibility(View.VISIBLE);
            }
        }
    }

    private SwipeMenuCreator swipeMenuCreator = (leftMenu, rightMenu, position) -> {
        SwipeMenuItem deleteItem = new SwipeMenuItem(AddressBookListActivity.this)
                .setBackground(R.drawable.menu_selector_red)
                .setText(getString(R.string.delete))
                .setTextColor(Color.WHITE)
                .setWidth(PixelUtils.dp2px(AddressBookListActivity.this,70))
                .setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        rightMenu.addMenuItem(deleteItem);
    };

    private OnItemMenuClickListener mMenuItemClickListener = (menuBridge, position) -> {
        menuBridge.closeMenu();
        int direction = menuBridge.getDirection();
        if(direction == SwipeRecyclerView.RIGHT_DIRECTION) {
            BHAddressBook addressBook =  bookListAdapter.getData().get(position);
            addressBookViewModel.deleteAddress(AddressBookListActivity.this,addressBook);
        }
    };
}