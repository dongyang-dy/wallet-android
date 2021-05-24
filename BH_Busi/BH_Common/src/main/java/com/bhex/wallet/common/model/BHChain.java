package com.bhex.wallet.common.model;

/**
 * @author gongdongyang
 * 2020-10-27 15:41:24
 */
public class BHChain {
    public String chain;
    public String full_name;
    public boolean single_coin;
    public String logo;
    public BHChain() {
    }

    public BHChain(String chain, String full_name) {
        this.chain = chain;
        this.full_name = full_name;
    }
}
