package com.bhex.wallet.bh_main.my.enums;

/**
 * @author gongdongyang
 * 2020-11-18 10:39:32
 *
 */
public enum  BUSI_MY_TYPE {
    //修改安全密码(0,"修改安全密码"),
    /*备份助记词(0,"备份助记词"),
    备份私钥(1,"备份私钥"),
    备份KS(2,"备份Keystore"),
    公告(3,"公告"),
    帮助中心(4,"帮助中心"),
    关于我们(5,"关于我们"),
    设置(6,"设置");*/
    账户与安全(0,"账户与安全"),
    备份导出(1,"备份导出"),
    公告(2,"公告"),
    消息中心(3,"消息中心"),
    设置(4,"设置"),
    帮助中心(5,"帮助中心"),
    关于我们(6,"关于我们");


    //版本号(7,"版本号");
    public int index;
    public String label;

    BUSI_MY_TYPE(int index, String label) {
        this.index = index;
        this.label = label;
    }

    public static BUSI_MY_TYPE getType(int index){
        for (BUSI_MY_TYPE item : values()) {
            if(item.index!=index){
                continue;
            }
            return  item;
        }
        return null;
    }
}
