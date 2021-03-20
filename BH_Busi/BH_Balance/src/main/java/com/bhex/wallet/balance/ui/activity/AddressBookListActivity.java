package com.bhex.wallet.balance.ui.activity;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHAddressBook;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author gongdongyang
 */
@Route(path = ARouterConfig.Balance.Balance_address_list,name = "地址列表")
public class AddressBookListActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_address_book_list;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void addEvent() {

    }

    class AddressBookListAdapter extends BaseQuickAdapter<BHAddressBook, BaseViewHolder>{
        public AddressBookListAdapter(@Nullable List<BHAddressBook> data) {
            super(R.layout.item_address_book, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, BHAddressBook bhAddressBook) {

        }
    }
}