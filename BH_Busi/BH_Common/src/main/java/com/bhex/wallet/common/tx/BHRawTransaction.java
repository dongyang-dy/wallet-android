package com.bhex.wallet.common.tx;

import com.bhex.network.utils.JsonUtils;
import com.bhex.tools.constants.BHConstants;
import com.bhex.tools.utils.LogUtils;
import com.bhex.tools.utils.NumberUtil;
import com.bhex.wallet.common.cache.CacheCenter;
import com.bhex.wallet.common.cache.SymbolCache;
import com.bhex.wallet.common.enums.TRANSCATION_BUSI_TYPE;
import com.bhex.wallet.common.manager.BHUserManager;
import com.bhex.wallet.common.model.BHRates;
import com.bhex.wallet.common.model.BHToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author gongdongyang
 * 2020-11-4 23:47:17
 */
public class BHRawTransaction {
    public String chain_id = BHConstants.CHAIN_ID;
    //public String cu_number = "0";
    public TxFee fee;
    public String memo;
    public List<TxReq.TxMsg> msgs;
    public String sequence;

    public static BHRawTransaction createBaseTransaction(String sequence, String memo, BigInteger feeAmount){
        BHRawTransaction bhRawTransaction = new BHRawTransaction();
        bhRawTransaction.chain_id = BHConstants.CHAIN_ID;
        //转账手续费
        bhRawTransaction.fee = getTransactionFee(feeAmount);

        bhRawTransaction.memo = memo;
        bhRawTransaction.msgs = new ArrayList<>();
        bhRawTransaction.sequence = sequence;
        return bhRawTransaction;
    }


    public static TxFee getTransactionFee(BigInteger feeAmount){
        TxFee fee = new TxFee();
        fee.amount = new ArrayList<>();

        TxFee.TxCoin feeCoin = new TxFee.TxCoin();
        feeCoin.amount = feeAmount.toString(10);
        LogUtils.d("BHRawTransaction==>:","feeCoin.amount=="+feeCoin.amount);
        feeCoin.denom = BHConstants.BHT_TOKEN;
        fee.amount.add(feeCoin);
        //fee.gas = NumberUtil.mulExt("2",String.valueOf(Math.pow(10,6))).toString();
        fee.gas = BHUserManager.getInstance().getDefaultGasFee().gas;
        return  fee;
    }

    //创建转账msg
    public static List<TxReq.TxMsg> createTransferMsg( String to,String amount, String symbol){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        String from = BHUserManager.getInstance().getCurrentBhWallet().address;

        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);

        BigInteger double_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,bhToken.decimals)),amount);

        //开始创建一个交易TxMsg
        TxReq.TxMsg<TransactionMsg.TransferMsg> msg = new TxReq.TxMsg<TransactionMsg.TransferMsg>();
        msg.type = TRANSCATION_BUSI_TYPE.转账.getType();

        TransactionMsg.TransferMsg transferMsg = new TransactionMsg.TransferMsg();
        msg.value = transferMsg;

        transferMsg.amount = new ArrayList<>();

        //转账Amount
        TxFee.TxCoin coin = new TxFee.TxCoin();
        coin.denom = symbol;
        coin.amount = double_amount.toString(10);

        transferMsg.amount.add(coin);


        transferMsg.from_address = from;
        transferMsg.to_address = to;

        tx_msg_list.add(msg);
        return tx_msg_list;
    }

    //创建提币交易
    public static List<TxReq.TxMsg> createwithDrawWMsg( String to,String withDrawAmount, String withDrawFeeAmount, BHToken withDrawToken, String hbc_symbol){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();


        String from = BHUserManager.getInstance().getCurrentBhWallet().address;

        //提币手续费
        //BHToken symbolBHToken = SymbolCache.getInstance().getBHToken(hbc_symbol);

        BigInteger double_gas_fee =  NumberUtil.mulExt(String.valueOf(Math.pow(10,withDrawToken.decimals)),withDrawFeeAmount);

        //提币数量
        BigInteger double_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,withDrawToken.decimals)),withDrawAmount);

        TxReq.TxMsg<TransactionMsg.WithdrawalMsg> msg = new TxReq.TxMsg<TransactionMsg.WithdrawalMsg>();
        msg.type = TRANSCATION_BUSI_TYPE.跨链提币.getType();
        TransactionMsg.WithdrawalMsg withdrawalMsg = new TransactionMsg.WithdrawalMsg();
        msg.value = withdrawalMsg;

        withdrawalMsg.from_cu = from;
        withdrawalMsg.to_multi_sign_address = to;
        withdrawalMsg.symbol = withDrawToken.symbol;
        withdrawalMsg.amount = double_amount.toString(10);
        withdrawalMsg.gas_fee = double_gas_fee.toString(10);
        withdrawalMsg.order_id = UUID.randomUUID().toString();

        tx_msg_list.add(msg);
        return  tx_msg_list;
    }

    //创建发行代币信息
    public static List<TxReq.TxMsg> createHrc20TokenWMsg(BHTokenRlease tokenRlease){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();
        //代币发行数据
        TxReq.TxMsg<BHTokenRlease> msg = new TxReq.TxMsg<BHTokenRlease>();
        msg.value = tokenRlease;
        msg.type = TRANSCATION_BUSI_TYPE.代币发行.getType();

        tx_msg_list.add(msg);
        return  tx_msg_list;
    }

    //创建映射兑换信息
    public static List<TxReq.TxMsg> createSwapMappingMsg(String issue_symbol, String coin_symbol, String swap_amount){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        String fromUser = BHUserManager.getInstance().getCurrentBhWallet().address;

        //mappingAmount
        BHToken bhToken = CacheCenter.getInstance().getTokenMapCache().getBHToken(coin_symbol);
        BigInteger double_swap_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,bhToken.decimals)),swap_amount);

        TxReq.TxMsg<TransactionMsg.MappingSwapMsg> txMsg = new TxReq.TxMsg();

        txMsg.type = TRANSCATION_BUSI_TYPE.映射.getType();

        TransactionMsg.MappingSwapMsg mappingSwapMsg = new TransactionMsg.MappingSwapMsg();
        txMsg.value = mappingSwapMsg;

        mappingSwapMsg.from = fromUser;
        mappingSwapMsg.issue_symbol = issue_symbol;

        TxFee.TxCoin txCoin = new TxFee.TxCoin();
        txCoin.amount = double_swap_amount.toString(10);
        txCoin.denom = coin_symbol;
        mappingSwapMsg.coins.add(txCoin);

        tx_msg_list.add(txMsg);
        return  tx_msg_list;
    }


    //流动性
    public static BHRawTransaction createBHRaw_transcation( JsonArray jsonArray, BigInteger feeAmount, String sequence){
        BHRawTransaction bhRawTransaction = new BHRawTransaction();
        bhRawTransaction.memo = "";
        bhRawTransaction.sequence = sequence;
        bhRawTransaction.msgs = new ArrayList<>();

        for(int i=0;i<jsonArray.size();i++){
            TxReq.TxMsg<JsonObject> txMsg = new TxReq.TxMsg();
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            if(!jsonObject.get("type").isJsonObject()){
                txMsg.type = jsonObject.get("type").getAsString();
            }

            if(!jsonObject.get("type").isJsonObject()){
                txMsg.value = jsonObject.get("value").getAsJsonObject();
            }

            bhRawTransaction.msgs.add(txMsg);
        }


        bhRawTransaction.fee = getTransactionFee(feeAmount);
        return bhRawTransaction;
    }

    //创建跨链地址生成信息
    public static List<TxReq.TxMsg> createGenerateAddressMsg(String symbol){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        String from_address = BHUserManager.getInstance().getCurrentBhWallet().getAddress();
        TxReq.TxMsg<TransactionMsg.KeyGenMsg> msg = new TxReq.TxMsg<TransactionMsg.KeyGenMsg>();
        msg.type = TRANSCATION_BUSI_TYPE.跨链地址生成.getType();
        TransactionMsg.KeyGenMsg keyGenMsg = new TransactionMsg.KeyGenMsg();
        msg.value = keyGenMsg;

        keyGenMsg.from = from_address;
        keyGenMsg.to = from_address;
        keyGenMsg.order_id = UUID.randomUUID().toString();
        keyGenMsg.symbol = symbol;

        tx_msg_list.add(msg);
        return tx_msg_list;
    }

    //创建委托
    public static List<TxReq.TxMsg> createDoEntrustMsg( String validatorAddress,String delegator_amount,String symbol){

        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        //委托数量
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);
        BigInteger double_delegator_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,bhToken.decimals)),delegator_amount);

        //委托人
        String delegatorAddress = BHUserManager.getInstance().getCurrentBhWallet().address;

        //开始创建一个委托TxMsg
        TxReq.TxMsg<TransactionMsg.DoEntrustMsg> msg = new TxReq.TxMsg<TransactionMsg.DoEntrustMsg>();
        msg.type = TRANSCATION_BUSI_TYPE.委托.getType();

        TransactionMsg.DoEntrustMsg doEntrustMsg = new TransactionMsg.DoEntrustMsg();
        msg.value = doEntrustMsg;

        //转账Amount
        TxFee.TxCoin coin = new TxFee.TxCoin();
        coin.denom = symbol;
        coin.amount = double_delegator_amount.toString(10);

        doEntrustMsg.amount = coin;


        doEntrustMsg.validator_address = validatorAddress;
        doEntrustMsg.delegator_address = delegatorAddress;

        tx_msg_list.add(msg);
        return tx_msg_list;
    }

    //解委托
    public static List<TxReq.TxMsg> createUnEntrustMsg(String validator_address, String un_delegator_amount, String symbol) {

        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        //委托数量
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);
        BigInteger double_un_delegator_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,bhToken.decimals)),un_delegator_amount);

        //委托人
        String delegator_address = BHUserManager.getInstance().getCurrentBhWallet().address;

        TxReq.TxMsg<TransactionMsg.DoEntrustMsg> msg = new TxReq.TxMsg<TransactionMsg.DoEntrustMsg>();

        msg.type = TRANSCATION_BUSI_TYPE.解委托.getType();
        TransactionMsg.DoEntrustMsg doEntrustMsg = new TransactionMsg.DoEntrustMsg();
        msg.value = doEntrustMsg;

        //转账Amount
        TxFee.TxCoin coin = new TxFee.TxCoin();
        coin.denom = symbol;
        coin.amount = double_un_delegator_amount.toString(10);

        doEntrustMsg.amount = coin;


        doEntrustMsg.validator_address = validator_address;
        doEntrustMsg.delegator_address = delegator_address;
        tx_msg_list.add(msg);
        return tx_msg_list;
    }

    //提取收益
    public static List<TxReq.TxMsg> createRewardMsg(List<TransactionMsg.ValidatorMsg>list){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        //开始创建一个交易TxMsg
        for(TransactionMsg.ValidatorMsg item:list){
            TxReq.TxMsg<TransactionMsg.ValidatorMsg> msg = new TxReq.TxMsg<TransactionMsg.ValidatorMsg>();
            msg.type = TRANSCATION_BUSI_TYPE.提取收益.getType();
            msg.value = item;
            tx_msg_list.add(msg);
        }
        return tx_msg_list;
    }

    //复投分红
    public static List<TxReq.TxMsg> createReDoEntrustMsg(List<TransactionMsg.ValidatorMsg>validatorMsgs,
                                                   List<TransactionMsg.DoEntrustMsg> doEntrustMsgs){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        //开始创建一个提取收益交易
        for(TransactionMsg.ValidatorMsg item:validatorMsgs){
            TxReq.TxMsg<TransactionMsg.ValidatorMsg> msg = new TxReq.TxMsg<TransactionMsg.ValidatorMsg>();
            msg.type = TRANSCATION_BUSI_TYPE.提取收益.getType();
            msg.value = item;
            tx_msg_list.add(msg);
        }

        //构建委托交易
        for(TransactionMsg.DoEntrustMsg item:doEntrustMsgs){
            //开始创建一个委托TxMsg
            TxReq.TxMsg<TransactionMsg.DoEntrustMsg> msg = new TxReq.TxMsg<TransactionMsg.DoEntrustMsg>();
            msg.type = TRANSCATION_BUSI_TYPE.委托.getType();
            msg.value = item;
            //转账Amount
            tx_msg_list.add(msg);
        }
        return  tx_msg_list;
    }

    //投票
    public static List<TxReq.TxMsg> createVoteMsg(String delegatorAddress, String option, String proposalId){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        //开始创建一个交易TxMsg
        TxReq.TxMsg<TransactionMsg.VetoMsg> msg = new TxReq.TxMsg<TransactionMsg.VetoMsg>();

        msg.type = TRANSCATION_BUSI_TYPE.治理提案投票.getType();

        TransactionMsg.VetoMsg vetoMsg = new TransactionMsg.VetoMsg();
        msg.value = vetoMsg;

        vetoMsg.voter = delegatorAddress;

        vetoMsg.option = option;
        vetoMsg.proposal_id = proposalId;

        tx_msg_list.add(msg);

        return  tx_msg_list;
    }

    //质押
    public static List<TxReq.TxMsg> createPledgeMsg( String proposalId,String pledge_amount,String symbol){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        String delegator_address = BHUserManager.getInstance().getCurrentBhWallet().address;

        //质押数量
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);
        BigInteger double_pledge_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,bhToken.decimals)),pledge_amount);

        //开始创建一个交易TxMsg
        TxReq.TxMsg<TransactionMsg.PledgeMsg> msg = new TxReq.TxMsg<TransactionMsg.PledgeMsg>();
        msg.type = TRANSCATION_BUSI_TYPE.治理提案质押.getType();

        TransactionMsg.PledgeMsg pledgeMsg = new TransactionMsg.PledgeMsg();
        msg.value = pledgeMsg;

        pledgeMsg.amount = new ArrayList<>();

        //转账Amount
        TxFee.TxCoin coin = new TxFee.TxCoin();
        coin.denom = symbol;
        coin.amount = double_pledge_amount.toString(10);

        pledgeMsg.amount.add(coin);


        pledgeMsg.depositor = delegator_address;
        pledgeMsg.proposal_id = proposalId;

        tx_msg_list.add(msg);

        return  tx_msg_list;
    }

    //治理提案
    public static List<TxReq.TxMsg> createProposalMsg(String type, String title,
                                                String description, String  proposal_amount,String symbol){
        List<TxReq.TxMsg> tx_msg_list = new ArrayList<>();

        String delegator_address = BHUserManager.getInstance().getCurrentBhWallet().address;

        //提案费用
        SymbolCache symbolCache = CacheCenter.getInstance().getSymbolCache();
        BHToken bhToken = symbolCache.getBHToken(symbol);
        BigInteger double_proposal_amount = NumberUtil.mulExt(String.valueOf(Math.pow(10,bhToken.decimals)),proposal_amount);

        TxReq.TxMsg<TransactionMsg.CreateProposalMsg> msg = new TxReq.TxMsg<TransactionMsg.CreateProposalMsg>();

        msg.type = TRANSCATION_BUSI_TYPE.发起治理提案.getType();
        TransactionMsg.CreateProposalMsg createProposalMsg = new TransactionMsg.CreateProposalMsg();
        msg.value = createProposalMsg;

        createProposalMsg.initial_deposit = new ArrayList<>();

        //转账Amount
        TxFee.TxCoin coin = new TxFee.TxCoin();
        coin.denom = symbol;
        coin.amount = double_proposal_amount.toString(10);
        createProposalMsg.initial_deposit.add(coin);
        createProposalMsg.proposer = delegator_address;
        TransactionMsg.CreateProposalMsg.ProposalContent content = new TransactionMsg.CreateProposalMsg.ProposalContent();
        content.type = type;
        TransactionMsg.CreateProposalMsg.ProposalValue value = new TransactionMsg.CreateProposalMsg.ProposalValue();
        value.description = description;
        value.title = title;
        content.value = value;
        createProposalMsg.content = content;
        return  tx_msg_list;
    }
}
