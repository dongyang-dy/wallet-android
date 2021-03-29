package com.bhex.wallet.bh_main.my.presenter;

import android.content.Context;

import com.bhex.wallet.common.base.BaseActivity;
import com.bhex.wallet.common.base.BasePresenter;
import com.bhex.tools.constants.BHConstants;
import com.bhex.wallet.bh_main.my.model.CurrencyItem;
import com.bhex.wallet.common.enums.CURRENCY_TYPE;
import com.bhex.wallet.common.event.CurrencyEvent;
import com.bhex.wallet.common.manager.CurrencyManager;
import com.bhex.wallet.common.manager.MMKVManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/18
 * Time: 19:05
 */
public class RatePresenter extends BasePresenter {

    public RatePresenter(BaseActivity activity) {
        super(activity);
    }

    public List<CurrencyItem> getAllCurrency(Context context){
        //String currency_name = MMKVManager.getInstance().mmkv().decodeString(BHConstants.CURRENCY_USED, CURRENCY_TYPE.USD.shortName);
        String currency_name = CurrencyManager.getInstance().loadCurrency(context);
        List<CurrencyItem> list = new ArrayList<>();
        CURRENCY_TYPE []items = CURRENCY_TYPE.values();
        for (int i = 0; i <items.length ; i++) {
            CurrencyItem currencyItem = new CurrencyItem((i+1),items[i].name,items[i].shortName,false);

            if(currencyItem.shortName.equalsIgnoreCase(currency_name)){
                currencyItem.selected = true;
            }

            list.add(currencyItem);

        }


        return list;
    }

    public int getOldPosition(List<CurrencyItem> list){
        int index = -1;
        for(int i=0;i<list.size();i++){
            if(list.get(i).selected){
                index = i;
                break;
            }
        }
        return index;
    }
}
