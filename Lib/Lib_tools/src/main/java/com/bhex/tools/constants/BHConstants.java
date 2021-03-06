package com.bhex.tools.constants;

import com.bhex.tools.BuildConfig;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/8
 * Time: 17:51
 */
public class BHConstants {

    public static final String SP_NAME = "SP_BH";

    public static final String FRIST_BOOT = "frist_boot";
    //"com.jrmf360.action.ENTER"
    public static final String  MAIN_PATH = BuildConfig.K_APPLICATION_ID +".action.main";

    //选择的货币
    public static final String CURRENCY_USED = "currency";

    public static final int STATUS_COLOR_WHITE = 1;
    public static final int STATUS_COLOR_BLUE = 2;
    public static final int STATUS_COLOR_TRANS = 3;
    public static final int STATUS_COLOR_OTHER = -1;

    public static final String BACKUP_TEXT = "BACKUP_TEXT";
    public static final String BACKUP = "1";
    public static final String LATER_BACKUP = "2";

    public static final String COIN_DEFAULT_LIST = "hbc_btc_eth_trx";

    public static final String TOKEN_DEFAULT_LIST = "token_default_list";


    //public static final String SYMBOL_DEFAULT_LIST = "hbc,btc,eth,usdt";

    public static final String SYMBOL_RATE_KEY = "SYMBOL_RATE_KEY";

    public static final String SYMBOL_DEFAULT_KEY = "SYMBOL_DEFAULT_KEY";

    public static final String SYMBOL_REMOVE_KEY = "SYMBOL_REMOVE_KEY";


    public static final String BHT_TOKEN = "hbc";
    public static final String HBTC = "hbtc";

    public static final int BHT_DEFAULT_DECIMAL = 4;

    public static final int PAGE_SIZE = 30;

    public static final double BHT_GAS_PRICE = Math.pow(10,12);

    public static final double BHT_DECIMALS = Math.pow(10,18);
    //默认手续费
    public static final String BHT_DEFAULT_FEE = "0.01";

    //链内操作
    public static final int INNER_LINK= 1;
    //跨链操作
    //public static final int CROSS_LINK= 2;


    public static final byte []BH = new byte[]{5,-54};
    //public static final byte []HBT = new byte[]{2, 16, 103};
    public static final byte []HBT = new byte[]{2, 16, 66};

    public static final String BH_MEMO="";
    //验证人-有效
    public static final int VALIDATOR_VALID= 1;
    //验证人-无效
    public static final int VALIDATOR_INVALID= 0;

    //夜间模式
    public static final String THEME_MODEL ="theme_model";
    //LiveDataBus 订阅账户信息
    public static final String Label_Account = "accountLiveData";
    //助记词备份
    public static final String Label_Mnemonic_Back = "mnemonicBackLiveData";


    public static final String VETO_OPTION_YES = "Yes";
    public static final String VETO_OPTION_NO = "No";
    public static final String VETO_OPTION_ABSTAIN = "Abstain";
    public static final String VETO_OPTION_NOWITHVETO = "NoWithVeto";
    public static final String TRANSCTION_MODE = "sync";
    public static final String TRANSCTION_MODE_BLOCK = "block";
    public static final String EMAIL = "hbtcwallet@hbtc.com";
    public static final String CHAIN_ID = "hbtc-testnet";

    public static String TextProposalType="hbtcchain/gov/TextProposal";

    //指纹识别
    public static final String FINGER_PWD_KEY = "fingerpwdkey";

    public static final String INPUT_PASSWORD = "inputPwd";

    public static final String WALLET_ADDRESS = "wallet_address";


    public static final int PRIVATE_KEY_LENGTH = 64;

    public static final int PUBLIC_KEY_LENGTH = 128;

    public static final String API_BASE_URL = "https://explorer.hbtcchain.io/";
    public static final String MARKET_URL = "https://dex.hbtcchain.io";
    //public static final String API_BASE_URL = "http://hbtc.yym.plus/";
    //public static final String MARKET_URL = "http://swap.yym.plus";
}
