package com.bhex.wallet.balance.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bhex.lib_qr.XQRCode;
import com.bhex.lib_qr.util.QRCodeAnalyzeUtils;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.network.utils.ToastUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.PathUtils;
import com.bhex.tools.utils.ToolUtils;
import com.bhex.wallet.balance.R;
import com.bhex.wallet.balance.helper.BHTokenHelper;
import com.bhex.wallet.balance.ui.fragment.ChooseChainFragment;
import com.bhex.wallet.balance.ui.viewhodler.AddAddressVH;
import com.bhex.wallet.balance.viewmodel.AddressBookViewModel;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.ConfigMapCache;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.config.ARouterConfig;
import com.bhex.wallet.common.db.entity.BHAddressBook;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHChain;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.ui.activity.BHQrScanActivity;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import java.util.List;

/**
 * @author gongdongyagn
 * 添加地址
 * 2021-3-20 15:33:34
 */
@Route(path = ARouterConfig.Balance.Balance_address_add,name = "添加地址列表")
public class AddAddressActivity extends BaseActivity {

    @Autowired(name = BHConstants.CHAIN)
    public String chain;
    //BHToken bhChainToken;

    private AddAddressVH addAddressVH;
    private AddressBookViewModel addressBookViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_address;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        addAddressVH = new AddAddressVH(this);

        //BHToken bhSymbolToken = SymbolCache.getInstance().getBHToken(symbol);
        //bhChainToken = SymbolCache.getInstance().getBHToken(bhSymbolToken.chain);

        addAddressVH.setChainIcon(chain);
    }

    @Override
    protected void addEvent() {
        findViewById(R.id.btn_save).setOnClickListener(this::saveAddressAction);

        addressBookViewModel = ViewModelProviders.of(this).get(AddressBookViewModel.class);
        addressBookViewModel.addLiveData.observe(this, ldm -> {
            updateStatus(ldm);
        });

        if(!chain.equalsIgnoreCase(BHConstants.BHT_TOKEN)){
            addAddressVH.layout_choose_chain.setOnClickListener(v -> {
                List<BHChain> bhChainList = BHTokenHelper.getAllBHChainList();
                ChooseChainFragment.getInstance(bhChainList,chain,AddAddressActivity.this::chooseChainAction)
                        .show(getSupportFragmentManager(),ChooseChainFragment.class.getName());
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理二维码扫描结果
        if (requestCode == BHQrScanActivity.REQUEST_CODE ) {
            if(resultCode == RESULT_OK){
                //处理扫描结果（在界面上显示）
                String qrCode  = data.getExtras().getString(XQRCode.RESULT_DATA);
                if(TextUtils.isEmpty(qrCode)){
                    return;
                }
                addAddressVH.inp_address.setText(qrCode);
                addAddressVH.inp_address.setSelection(qrCode.length());
            }else if(resultCode == BHQrScanActivity.REQUEST_IMAGE){
                getAnalyzeQRCodeResult(data.getData());
            }
        }
    }

    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(this, uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                if(TextUtils.isEmpty(result)){
                    return;
                }
                addAddressVH.inp_address.setText(result);
                addAddressVH.inp_address.setSelection(result.length());
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.showToast(getResources().getString(R.string.encode_qr_fail));
            }
        });
    }

    //保存地址
    private void saveAddressAction(View view) {
        boolean flag = addAddressVH.verifyInputAction(chain);
        if(!flag){
            return;
        }
        BHAddressBook addressBook = new BHAddressBook();
        addressBook.chain = chain;
        //addressBook.symbol = symbol;
        addressBook.wallet_address = BHUserManager.getInstance().getCurrentBhWallet().address;
        addressBook.address = addAddressVH.inp_address.getText().toString().trim();
        addressBook.address_name = addAddressVH.inp_address_name.getText().toString().trim();
        String v_address_remark = addAddressVH.inp_address_remark.getText().toString().trim();
        if(!TextUtils.isEmpty(v_address_remark)){
            addressBook.address_remark = v_address_remark;
        }
        addressBookViewModel.saveAddress(this,addressBook);
    }

    private void updateStatus(LoadDataModel<Long> ldm) {
        if(ldm.getLoadingStatus()== LoadingStatus.SUCCESS){
            if(ldm.getData()>0){
                ToastUtils.showToast(getString(R.string.address_exists));
            }else{
                ToastUtils.showToast(getString(R.string.save_success));
                finish();
            }
        }else{
            ToastUtils.showToast(getString(R.string.save_fail));
        }
    }


    private void chooseChainAction(String chooseChain) {
        //bhChainToken = SymbolCache.getInstance().getBHToken(chooseChain);
        //BHChain bhChain = BHTokenHelper.getBHChain(chooseChain);
        chain = chooseChain;
        addAddressVH.setChainIcon(chooseChain);
    }


}