package com.bhex.wallet.common.enums;

import android.content.Context;

import com.bhex.wallet.common.R;


/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/18
 * Time: 16:28 hbtcchain/transfer/MsgDeposit
 * 交易类型
 *
 */
public enum TRANSCATION_BUSI_TYPE {
    转账("bhexchain/transfer/MsgSend"),
    委托("bhexchain/MsgDelegate"),
    解委托("bhexchain/MsgUndelegate"),
    跨链地址生成("bhexchain/keygen/MsgKeyGen"),
    提取收益("bhexchain/MsgWithdrawDelegationReward"),
    跨链充值("bhexchain/transfer/MsgDeposit"),
    跨链提币("bhexchain/transfer/MsgWithdrawal"),
    发起治理提案("bhexchain/gov/MsgSubmitProposal"),
    治理提案质押("bhexchain/gov/MsgDeposit"),
    治理提案投票("bhexchain/gov/MsgVote"),
    复投分红("bhexchain/reinvest"),
    代币发行("bhexchain/hrc20/MsgNewToken"),
    映射("bhexchain/mapping/MsgMappingSwap"),
    兑换_输入确定("bhexchain/openswap/MsgSwapExactIn"),
    兑换_输出确定("bhexchain/openswap/MsgSwapExactOut"),
    添加流动性("bhexchain/openswap/MsgAddLiquidity"),
    移除流动性("bhexchain/openswap/MsgRemoveLiquidity"),
    撤单("bhexchain/openswap/MsgCancelLimitSwap"),
    限价单兑换("bhexchain/openswap/MsgLimitSwap"),
    Other("other");

    private String type;
    private String label;

    TRANSCATION_BUSI_TYPE(String type) {
        this.type = type;
    }

    TRANSCATION_BUSI_TYPE(String type, String label) {
        this.type = type;
        this.label = label;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static void init(Context context){
        转账.label = context.getResources().getString(R.string.transfer2);
        委托.label = context.getResources().getString(R.string.do_entrust);
        解委托.label = context.getResources().getString(R.string.relieve_entrust);
        跨链地址生成.label = context.getResources().getString(R.string.crosslink_address);
        提取收益.label = context.getResources().getString(R.string.withdraw_reward);
        跨链充值.label = context.getResources().getString(R.string.cross_deposit);
        跨链提币.label = context.getResources().getString(R.string.cross_chian_trans_out);
        发起治理提案.label= context.getResources().getString(R.string.initiate_proposal);
        治理提案质押.label=context.getResources().getString(R.string.pledge_proposal);
        治理提案投票.label=context.getResources().getString(R.string.vote_proposal);
        复投分红.label = context.getResources().getString(R.string.reinvset_share);
        映射.label = context.getResources().getString(R.string.mapping_swap);
        兑换_输入确定.label = context.getResources().getString(R.string.swap);
        兑换_输出确定.label = context.getResources().getString(R.string.swap);
        添加流动性.label = context.getResources().getString(R.string.add_liquidity);
        移除流动性.label = context.getResources().getString(R.string.remove_liquidity);
        撤单.label = context.getResources().getString(R.string.cancel_limit_swap);
        限价单兑换.label = context.getResources().getString(R.string.limit_swap);
        Other.label = context.getResources().getString(R.string.other);
    }

    public static String getValue(String type) {
        TRANSCATION_BUSI_TYPE[] transcationBusiTypes = values();
        for (TRANSCATION_BUSI_TYPE item : transcationBusiTypes) {
            if (item.getType().equals(type)) {
                return item.label;
            }
        }
        return Other.label;
    }

}
