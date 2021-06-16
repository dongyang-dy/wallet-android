package com.bhex.wallet.common.viewmodel;

import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bhex.network.RxSchedulersHelper;
import com.bhex.network.app.BaseApplication;
import com.bhex.network.base.LoadDataModel;
import com.bhex.network.base.LoadingStatus;
import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.network.observer.BHBaseObserver;
import com.bhex.network.observer.BHProgressObserver;
import com.bhex.network.observer.SimpleObserver;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.crypto.CryptoUtil;
import com.bhex.tools.crypto.HexUtils;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.MD5;
import com.bhex.wallet.common.crypto.wallet.HWallet;
import com.bhex.wallet.common.crypto.wallet.HWalletFile;
import com.bhex.wallet.common.db.AppDataBase;
import com.bhex.wallet.common.db.dao.BHWalletDao;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.enums.BH_BUSI_TYPE;
import com.bhex.wallet.common.helper.BHWalletHelper;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.manager.SequenceManager;
import com.bhex.wallet.common.utils.BHKey;
import com.bhex.wallet.common.utils.BHWalletUtils;
import com.bhex.wallet.common.utils.LiveDataBus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.ObjectMapperFactory;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/6
 * Time: 11:00
 */
public class WalletViewModel extends ViewModel {

    private BHWalletDao bhWalletDao;

    public MutableLiveData<LoadDataModel> mutableLiveData  = new MutableLiveData<>();

    public MutableLiveData<LoadDataModel<List<BHWallet>>> mutableWallentLiveData = new MutableLiveData<>();

    public MutableLiveData<LoadDataModel<BHWallet>> walletLiveData = new MutableLiveData<>();

    public MutableLiveData<LoadDataModel<BHWallet>> deleteLiveData = new MutableLiveData<>();

    public MutableLiveData<LoadDataModel> pwdVerifyLiveData = new MutableLiveData<>();


    private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public WalletViewModel() {
        bhWalletDao = AppDataBase.getInstance(BaseApplication.getInstance()).bhWalletDao();
    }

    /**
     * 创建助记词并保存本地
     */
    public void generateMnemonic(BaseActivity activity,String name, String pwd){

        BHProgressObserver pbo = new BHProgressObserver<BHWallet>(activity) {
            @Override
            public void onSuccess(BHWallet bhWallet) {
                LoadDataModel loadDataModel = new LoadDataModel("");
                BHUserManager.getInstance().setCurrentBhWallet(bhWallet);
                mutableLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,"");
                mutableLiveData.postValue(loadDataModel);
            }
        };


        Observable.create((emitter)->{
            try{
                BHWallet bhWallet = BHWalletUtils.generateMnemonic(name,pwd);
                int maxId = bhWalletDao.loadMaxId();

                bhWallet.id = maxId+1;
                int id = bhWalletDao.insert(bhWallet).intValue();

                //设置默认钱包
                //把所有设置非默认
                int res = bhWalletDao.updateNoDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
                //把bh_id设置默认
                res = bhWalletDao.update(id,BH_BUSI_TYPE.默认托管单元.getIntValue());
                BHUserManager.getInstance().setCurrentBhWallet(bhWallet);

                //获取所有钱包
                List<BHWallet> list =  bhWalletDao.loadAll();
                BHUserManager.getInstance().setAllWallet(list);
                //SequenceManager.getInstance().initSequence();
                emitter.onNext(bhWallet);
                emitter.onComplete();
            }catch (Exception e){
                e.printStackTrace();
                emitter.onError(e);
            }
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    /**
     * 获取所有的钱包
     */
    public void loadWallet(FragmentActivity activity){
        SimpleObserver<List<BHWallet>> observer = new SimpleObserver<List<BHWallet>>() {
            @Override
            public void onNext(List<BHWallet> bhWalletList) {
                super.onNext(bhWalletList);
                LoadDataModel<List<BHWallet>> loadDataModel = new LoadDataModel<>(bhWalletList);
                mutableWallentLiveData.setValue(loadDataModel);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        };

        Observable.create((ObservableOnSubscribe<List<BHWallet>>)emitter -> {
            List<BHWallet> list = bhWalletDao.loadAll();
            if(list!=null && list.size()>0){
                BHUserManager.getInstance().setAllWallet(list);
            }
            emitter.onNext(list);
            emitter.onComplete();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);

    }

    /**
     * 更新钱包的状态
     */
    public void updateWallet(AppCompatActivity activity,BHWallet bhWallet,int bh_id,int isDefault,boolean isShowDialog){
        BHProgressObserver pbo = new BHProgressObserver<String>(activity,false) {
            @Override
            protected void onSuccess(String str) {
                LoadDataModel loadDataModel = new LoadDataModel("");
                bhWallet.isDefault = BH_BUSI_TYPE.默认托管单元.getIntValue();
                BHUserManager.getInstance().setCurrentBhWallet(bhWallet);
                //
                SequenceManager.getInstance().initSequence();
                mutableLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel();
                mutableLiveData.postValue(loadDataModel);
            }
        };


        Observable.create((ObservableOnSubscribe<String>)emitter -> {
            //把所有设置非默认
            int res = bhWalletDao.updateNoDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
            //把bh_id设置默认
            res = bhWalletDao.update(bh_id,isDefault);

            List<BHWallet> list = bhWalletDao.loadAll();
            BHUserManager.getInstance().setAllWallet(list);
            emitter.onNext("");
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    /**
     * 修改用户名称
     * @param fragment
     * @param bhWallet
     */
    public void updateWalletName(Fragment fragment, BHWallet bhWallet){
        BHBaseObserver pbo = new BHBaseObserver<String>() {
            @Override
            protected void onSuccess(String str) {
                LoadDataModel loadDataModel = new LoadDataModel("");
                mutableLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel();
                mutableLiveData.postValue(loadDataModel);
            }
        };


        Observable.create((ObservableOnSubscribe<String>)emitter -> {
            //把所有设置非默认
            //把bh_id设置默认
            int res = bhWalletDao.update(bhWallet);
            //获取所有钱包
            List<BHWallet> list = bhWalletDao.loadAll();
            BHUserManager.getInstance().setAllWallet(list);
            emitter.onNext("");
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(fragment)))
                .subscribe(pbo);
    }


    /**
     *
     * @param activity
     * @param bh_id
     */
    public void deleteWallet(AppCompatActivity activity,int bh_id){
        BHProgressObserver pbo = new BHProgressObserver<String>(activity) {
            @Override
            protected void onSuccess(String str) {
                //ToastUtils.showToast(str);
                LoadDataModel loadDataModel = new LoadDataModel("");
                deleteLiveData.postValue(loadDataModel);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,errorMsg);
                deleteLiveData.postValue(loadDataModel);
            }
        };

        Observable.create((ObservableOnSubscribe<String>)emitter -> {
            //把所有设置非默认
            bhWalletDao.deleteWallet(bh_id);
            //更新所有用钱包
            //更新当前默认钱包
            List<BHWallet> allBhWallet = bhWalletDao.loadAll();
            if(allBhWallet!=null && allBhWallet.size()>0){
                BHUserManager.getInstance().setAllWallet(allBhWallet);
            }
            emitter.onNext("1");
            emitter.onComplete();
          }
        ).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    /**
     * @param activity
     * @param name
     * @param pwd
     */
    public void importMnemonic(BaseActivity activity,List<String> words, String name, String pwd){
        BHProgressObserver pbo = new BHProgressObserver<BHWallet>(activity) {
            @Override
            public void onSuccess(BHWallet bhWallet) {
                if(bhWallet==null || TextUtils.isEmpty(bhWallet.address)){
                    LoadDataModel loadDataModel = new LoadDataModel(1,"");
                    mutableLiveData.postValue(loadDataModel);
                }else{
                    LoadDataModel loadDataModel = new LoadDataModel();
                    BHUserManager.getInstance().setCurrentBhWallet(bhWallet);
                    SequenceManager.getInstance().initSequence();
                    mutableLiveData.postValue(loadDataModel);
                }
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel loadDataModel = new LoadDataModel(code,"");
                mutableLiveData.postValue(loadDataModel);
            }

        };

        Observable.create((emitter)->{
            try{
                //验证托管单元地址是否存在
                String bh_address = BHWalletUtils.memonicToAddress(words);
                //判断助记词是否已经导入过
                boolean isWalletExist = BHWalletHelper.isExistBHWallet(bh_address);
                if(isWalletExist){
                    //删除当前钱包
                    bhWalletDao.deleteByAddress(bh_address);
                }
                BHWallet bhWallet = createWallet(words,name,pwd);
                emitter.onNext(bhWallet);
                emitter.onComplete();
            }catch (Exception e){
                e.printStackTrace();
                emitter.onError(e);
            }
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    //创建
    private BHWallet createWallet(List<String> words, String name, String pwd){
        BHWallet bhWallet = BHWalletUtils.importMnemonic(BHWalletUtils.BH_CUSTOM_TYPE,words,name,pwd);
        int maxId = bhWalletDao.loadMaxId();
        bhWallet.isBackup = BH_BUSI_TYPE.已备份.getIntValue();
        bhWallet.id = maxId+1;
        int id = bhWalletDao.insert(bhWallet).intValue();

        //设置默认钱包
        //把所有设置非默认
        int res = bhWalletDao.updateNoDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
        //把bh_id设置默认
        res = bhWalletDao.update(id,BH_BUSI_TYPE.默认托管单元.getIntValue());

        //更新当前默认钱包
        List<BHWallet> allBhWallet = bhWalletDao.loadAll();
        if(allBhWallet!=null && allBhWallet.size()>0){
            BHUserManager.getInstance().setAllWallet(allBhWallet);
        }
        return bhWallet;
    }

    /**
     * 导入私钥
     * @param name
     * @param pwd
     */
    public void importPrivateKey(BaseActivity activity,String name, String pwd) {
        BHProgressObserver pbo = new BHProgressObserver<BHWallet>(activity) {
            @Override
            public void onSuccess(BHWallet bhWallet) {
                //SequenceManager.getInstance().initSequence();
                if(bhWallet==null || TextUtils.isEmpty(bhWallet.address)){
                    LoadDataModel loadDataModel = new LoadDataModel(1,"");
                    mutableLiveData.postValue(loadDataModel);
                }else{
                    LoadDataModel loadDataModel = new LoadDataModel("");
                    mutableLiveData.postValue(loadDataModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                LoadDataModel loadDataModel = new LoadDataModel(LoadingStatus.ERROR,"");
                mutableLiveData.postValue(loadDataModel);
            }
        };

        Observable.create((emitter)->{
            try{
                String privateKey = BHUserManager.getInstance().getCreateWalletParams().privateKey;
                //先判断地址是否存在
                String bh_address = BHWalletUtils.privatekeyToAddress(privateKey);
                boolean isWalletExist = BHWalletHelper.isExistBHWallet(bh_address);

                if(isWalletExist){
                    bhWalletDao.deleteByAddress(bh_address);
                }
                BHWallet bhWallet = BHWalletUtils.importPrivateKey(privateKey,name,pwd);
                int maxId = bhWalletDao.loadMaxId();
                bhWallet.isBackup = BH_BUSI_TYPE.已备份.getIntValue();
                bhWallet.id = maxId+1;
                int resId = bhWalletDao.insert(bhWallet).intValue();

                //设置默认钱包
                //把所有设置非默认
                resId = bhWalletDao.updateNoDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
                //把bh_id设置默认
                resId = bhWalletDao.update(bhWallet.id,BH_BUSI_TYPE.默认托管单元.getIntValue());

                //更新当前默认钱包
                List<BHWallet> allBhWallet = bhWalletDao.loadAll();
                if(allBhWallet!=null && allBhWallet.size()>0){
                    //BHUserManager.getInstance().setCurrentBhWallet(allBhWallet.get(0));
                    BHUserManager.getInstance().setAllWallet(allBhWallet);
                }

                emitter.onNext(bhWallet);
                emitter.onComplete();

            }catch (Exception e){
                e.printStackTrace();
                emitter.onError(e);
            }

        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    /**
     * 导入KeyStore文件
     * @param activity
     * @param keyStore
     * @param name
     * @param pwd
     */
    public void importKeyStore(BaseActivity activity,String keyStore,String name, String pwd){
        BHProgressObserver observer = new BHProgressObserver<String>(activity) {
            @Override
            protected void onSuccess(String status) {
                //SequenceManager.getInstance().initSequence();
                LoadDataModel ldm = new LoadDataModel(status);
                mutableLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                mutableLiveData.postValue(ldm);
            }


        };

        Observable.create(emitter -> {
            //判断地址是否存在
            String bh_address = BHWalletUtils.keyStoreToAddress(keyStore,pwd);
            if(BHWalletHelper.isExistBHWallet(bh_address)){
                bhWalletDao.deleteByAddress(bh_address);
            }

            Credentials credentials = BHWalletUtils.verifyKeystore(keyStore,pwd);
            BHWallet bhWallet = BHWalletUtils.importKeyStoreExt(credentials,name,pwd);
            //当前
            int maxId = bhWalletDao.loadMaxId();
            bhWallet.isBackup = BH_BUSI_TYPE.已备份.getIntValue();
            bhWallet.id = maxId+1;
            int resId = bhWalletDao.insert(bhWallet).intValue();

            //设置默认钱包
            //把所有设置非默认
            int res = bhWalletDao.updateNoDefault(BH_BUSI_TYPE.非默认托管单元.getIntValue());
            //把bh_id设置默认
            res = bhWalletDao.update(bhWallet.id,BH_BUSI_TYPE.默认托管单元.getIntValue());

            //更新当前默认钱包
            List<BHWallet> allBhWallet = bhWalletDao.loadAll();
            if(allBhWallet!=null && allBhWallet.size()>0){
                BHUserManager.getInstance().setAllWallet(allBhWallet);
            }
            emitter.onNext(BH_BUSI_TYPE.托管单元不存在.value);

            emitter.onComplete();

        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);
    }

    //删除钱包验证
    /*public void deleteWalletVerify(BaseActivity activity,String keyStore,String pwd){

    }*/

    //验证Keystore
    public void verifyKeystore(BaseActivity activity,String keyStore,String pwd,boolean isShowDialog, boolean isNeedToast){
        BHProgressObserver observer = new BHProgressObserver<String>(activity,isShowDialog,isNeedToast) {
            @Override
            protected void onSuccess(String result) {
                LoadDataModel ldm = new LoadDataModel(result);
                pwdVerifyLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                pwdVerifyLiveData.postValue(ldm);
            }
        };

        Observable.create(emitter -> {
            String bh_address = "" ;
            Credentials credentials = BHWalletUtils.verifyKeystore(keyStore,pwd);
            if(credentials!=null){
                bh_address = BHKey.getBhexUserDpAddress(credentials.getEcKeyPair());
            }
            if(BHWalletHelper.isExistBHWallet(bh_address)){
                emitter.onNext(BH_BUSI_TYPE.托管单元已存在.value);
            }else{
                emitter.onNext(BH_BUSI_TYPE.托管单元不存在.value);
            }
            //BHUserManager.getInstance().setTmpCredentials(credentials);
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(observer);
    }


    public void verifyKeystore(Fragment fragment,String keyStore,String pwd){
        BHBaseObserver observer = new BHBaseObserver<String>(false) {
            @Override
            protected void onSuccess(String result) {
                LoadDataModel ldm = new LoadDataModel(result);
                pwdVerifyLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                pwdVerifyLiveData.postValue(ldm);
            }
        };

        Observable.create(emitter -> {
            String bh_address = "" ;
            Credentials credentials = BHWalletUtils.verifyKeystore(keyStore,pwd);
            if(credentials!=null){
                bh_address = BHKey.getBhexUserDpAddress(credentials.getEcKeyPair());
                //存储密文
                BHWallet currentWallet = BHUserManager.getInstance().getCurrentBhWallet();
                currentWallet.password = MD5.generate(pwd);
                //加密后的私钥
                String encryptPK = CryptoUtil.encryptPK(credentials.getEcKeyPair().getPrivateKey(),pwd);
                currentWallet.privateKey = encryptPK;
                //加密后的助记词
                //解密助记词
                BHWallet bhWallet = BHUserManager.getInstance().getCurrentBhWallet();
                HWalletFile walletFile = objectMapper.readValue(bhWallet.keystorePath, HWalletFile.class);
                if(!TextUtils.isEmpty(walletFile.encMnemonic)){
                    String orgin_mnemonic = HWallet.解密_M(walletFile.encMnemonic,pwd,walletFile);
                    //加密助记词
                    String encrypt_mnemonic = CryptoUtil.encryptMnemonic(orgin_mnemonic,pwd);
                    bhWallet.setMnemonic(encrypt_mnemonic);
                }
            }
            emitter.onNext(bh_address);
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(fragment)))
                .subscribe(observer);
    }

    public void verifyChooseKeystore(Fragment fragment,String keyStore,String pwd){
        BHBaseObserver observer = new BHBaseObserver<String>(false) {
            @Override
            protected void onSuccess(String result) {
                LoadDataModel ldm = new LoadDataModel(result);
                pwdVerifyLiveData.postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel(code,errorMsg);
                pwdVerifyLiveData.postValue(ldm);
            }
        };

        Observable.create(emitter -> {
            String bh_address = "" ;
            Credentials credentials = BHWalletUtils.verifyKeystore(keyStore,pwd);
            if(credentials!=null){
                bh_address = BHKey.getBhexUserDpAddress(credentials.getEcKeyPair());
            }
            emitter.onNext(bh_address);
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(fragment)))
                .subscribe(observer);
    }

    /**
     * 修改密码
     * @param newPwd
     */
    public void updatePassword(BaseActivity activity,String oldPwd,String newPwd,BHWallet bhWallet) {
        BHProgressObserver pbo = new BHProgressObserver<BHWallet>(activity) {
            @Override
            public void onSuccess(BHWallet str) {
                LoadDataModel loadDataModel = new LoadDataModel(str);
                walletLiveData.postValue(loadDataModel);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                LoadDataModel loadDataModel = new LoadDataModel(LoadingStatus.ERROR,"");
                walletLiveData.postValue(loadDataModel);
            }
        };

        Observable.create((emitter)->{
            try{
                //更新KeyStore
                String newKeyStore = BHWalletUtils.updateKeyStore(bhWallet.keystorePath,oldPwd,newPwd);
                bhWallet.setMnemonic("");
                bhWallet.password="";
                bhWallet.setKeystorePath(newKeyStore);
                int res = bhWalletDao.update(bhWallet);
                if(res>0){
                    //更新钱包列表
                    List<BHWallet> list = bhWalletDao.loadAll();
                    BHUserManager.getInstance().setAllWallet(list);
                    emitter.onNext(bhWallet);
                }
                emitter.onComplete();
            }catch (Exception e){
                e.printStackTrace();
                emitter.tryOnError(e);
            }

        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

    /**
     * 备份助记词
     * @param activity
     * @param bhWallet
     */
    public void backupMnemonic(BaseActivity activity,BHWallet bhWallet) {
        BHProgressObserver pbo = new BHProgressObserver<String>(activity) {
            @Override
            protected void onSuccess(String str) {
                LoadDataModel ldm = new LoadDataModel("");
                bhWallet.isBackup = BH_BUSI_TYPE.已备份.getIntValue();
                BHUserManager.getInstance().setCurrentBhWallet(bhWallet);
                LiveDataBus.getInstance().with(BHConstants.Label_Mnemonic_Back,LoadDataModel.class).postValue(ldm);
            }

            @Override
            protected void onFailure(int code, String errorMsg) {
                super.onFailure(code, errorMsg);
                LoadDataModel ldm = new LoadDataModel();
                //walletLiveData.postValue(loadDataModel);
                LiveDataBus.getInstance().with(BHConstants.Label_Mnemonic_Back,LoadDataModel.class).postValue(ldm);
            }
        };


        Observable.create(emitter-> {
            //把bh_id设置默认
            int res = bhWalletDao.backupMnemonic(bhWallet.id);
            emitter.onNext("");
            emitter.onComplete();
        }).compose(RxSchedulersHelper.io_main())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(activity)))
                .subscribe(pbo);
    }

}
