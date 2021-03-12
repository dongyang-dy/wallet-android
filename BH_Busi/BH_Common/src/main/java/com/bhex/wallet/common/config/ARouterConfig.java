package com.bhex.wallet.common.config;

import com.alibaba.android.arouter.facade.annotation.Route;

/**
 * created by gongdongyang
 * on 2020/3/4
 */
public class ARouterConfig {
    public static class Main{
        public static final String main_mainindex ="/main/mainIndex";
        public static final String main_splash = "/main/splash";
    }

    /**
     * Common模块
     */
    public static class Common{
        //二维码扫描
        public static final String commom_scan_qr = "/common/scan_qr";
    }

    public static class My{
        /**
         * 我的
         */
        public static final String MINE_MAIN_PAGE = "/my/main";
        //语言设置
        public static final String My_Languae_Set = "/my/language/set";
        //修改密码
        public static final String My_Update_Password = "/my/update/password";

        //汇率设置
        public static final String My_Rate_setting = "/my/rate_set";

        //消息中心
        public static final String My_Message = "/my/message";
        //识别设置
        public static final String My_Recognition = "/my/recognition";
        //安全设置
        public static final String My_Security_Setting = "/my/security/setIndex";
        //账户与安全
        public static final String My_Account_Security = "/my/account/security";

        public static final String My_Account_Backup = "/my/account/backcup";
        //关于我们
        public static final String My_About = "/my/about";
        //设置
        public static final String My_Account_Setting = "/my/account/set";
        //账户详情
        public static final String ACCOUNT_DETAIL_PAGE = "/my/account/detail";

    }

    //托管单元-钱包账户
    public static class Trusteeship{
        public static final String Trusteeship_Add_Index = "/Trusteeship/add/index";

    }
    /**
     * 助记词模块
     */
    public static final String MNEMONIC_INDEX_PAGE = "/mnemonic/index";
    //托管单元管理
    public static final String ACCOUNT_MANAGER_PAGE = "/account/manager";

    //托管单元创建成功页面
    public static final String TRUSTEESHIP_CREATE_OK_PAGE = "/mnemonic/trusteeship/success";
    //助记词备份
    public static final String MNEMONIC_BACKUP = "/mnemonic/backup";
    //助记词验证
    public static final String MNEMONIC_VERIFY = "/mnemonic/verify";
    //托管单元创建
    public static final String TRUSTEESHIP_IMPORT_INDEX = "/mnemonic/trusteeship/index";

    //托管单元创建
    public static final String TRUSTEESHIP_MNEMONIC_FRIST= "/mnemonic/mnemonic/frist";

    public static final String TRUSTEESHIP_MNEMONIC_SECOND= "/mnemonic/mnemonic/second";

    public static final String TRUSTEESHIP_MNEMONIC_THIRD= "/mnemonic/mnemonic/third";
    //托管单元导入-KeyStore
    public static final String TRUSTEESHIP_IMPORT_KEYSTORE= "/mnemonic/import/keystore";
    //导出KeyStore
    public static final String TRUSTEESHIP_EXPORT_KEYSTORE = "/mnemonic/export/keystore";
    //托管单元导入
    public static final String TRUSTEESHIP_IMPORT_MNEMONIC = "/mnemonic/import/mnemonic";
    //私钥导入
    public static final String TRUSTEESHIP_IMPORT_PRIVATEKEY = "/mnemonic/import/privatekey";
    public static final String TRUSTEESHIP_IMPORT_PRIVATEKEY_NEXT = "/mnemonic/import/privatekey_next";
    //私钥导出
    public static final String TRUSTEESHIP_EXPORT_PRIVATEKEY = "/mnemonic/export/privatekey";

    //私钥导出提醒
    public static final String TRUSTEESHIP_EXPORT_INDEX = "/mnemonic/export/index";

    //public static final String TRUSTEESHIP_Delete_tip= "/mnemonic/delete_tip";
    //资产
    public static class Balance{
        //资产搜索
        public static final String Balance_Search = "/balance/search";
        //资产详情页
        public static final String Balance_Token_Detail = "/balance/token/detail";
        //充币
        public static final String Balance_transfer_in = "/balance/transfer/in";
        public static final String Balance_transfer_in_cross = "/balance/transfer/incross";
        //提币
        public static final String Balance_transfer_out = "/balance/transfer/out";
        public static final String Balance_transfer_out_cross = "/balance/transfer/outcross";
        //跨链地址生成页面
        public static final String Balance_cross_address = "/balance/cross_adress";

        //转账详情
        public static final String Balance_transcation_detail = "/balance/transcation/detail";
        public static final String Balance_transcation_view = "/balance/transcation/view";
        public static final String Balance_chain_tokens = "/balance/chain/tokens";
    }

    public static class Market{
        public static final String market_dex = "/market/dex";
        public static final String market_webview = "/market/webview";
        public static final String market_fragment_webview = "/market/fragment/webview";
    }




    //代币发行
    public static final String Token_Release = "/token/release";

    //验证人详情页
    public static class Validator{
        public static final String Validator_Detail = "/validator/detail";
        public static final String Do_Entrust = "/validator/do_entrust";
        public static final String Validator_Index = "/validator/index";
    }

    //治理提案
    public static class Proposal{
        public static final String Proposal_Detail = "/proposal/detail";
        public static final String Do_Pledge = "/proposal/do_pledge";
        public static final String Do_Veto = "/proposal/do_veto";
        public static final String Create_Proposal = "/proposal/create_proposal";
    }

    public static final String Market_exchange_coin = "/market/exchange/coin";
    public static final String Market_swap_mapping = "/market/swap/mapping";

    public static class Account {
        public static final String Account_Login_Password = "/account/login/password";
        public static final String Account_Login_Finger = "/account/login/finger";
    }

    public static class 中文{

        public static final String 帮助中心 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/";
        public static final String 公告 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/gong-gao/";
        public static final String HBTC_Wallet服务协议 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/qian-bao-app/hbtc-wallet-fu-wu-xie-yi";
        public static final String 联系我们 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/qian-bao-app/lian-xi-wo-men";
        public static final String 版本更新日志 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/qian-bao-app/ban-ben-geng-xin-ri-zhi";
        public static final String 审计报告 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/audit-report";
    }

    public static class 英文{
        public static final String 帮助中心 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/v/english/";
        public static final String 公告 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/v/english/announcement/";
        public static final String HBTC_Wallet服务协议 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/v/english/wallet-app/hbtc-wallet-terms-of-service";
        public static final String 联系我们 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/v/english/wallet-app/contact-us";
        public static final String 版本更新日志 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/v/english/wallet-app/version-update-log";
        public static final String 审计报告 = "https://hbtcwallet.gitbook.io/hbtc-chain-guide/v/english/";
    }
}
