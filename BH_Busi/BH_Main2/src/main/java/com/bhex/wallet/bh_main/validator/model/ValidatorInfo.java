package com.bhex.wallet.bh_main.validator.model;

import java.util.List;

/**
 * Created by BHEX.
 * User: zhou chang
 * Date: 2020/4/14
 */
public class ValidatorInfo {

    /**
     * address : string
     * operator_address : string
     * cu_address : string
     * proposer_priority : string
     * voting_power : string
     * all_voting_power : string
     * voting_power_proportion : string
     * self_delegate_amount : string
     * self_delegate_proportion : string
     * other_delegate_amount : string
     * other_delegate_proportion : string
     * up_time : string
     * jailed : true
     * status : 0
     * unbonding_height : 0
     * unbonding_time : 0
     * commission : {"rate":"string","max_rate":"string","max_change_rate":"string"}
     * description : {"moniker":"string","identity":"string","website":"string","details":"string","avatar":"string"}
     * last_blocks : [{"height":0,"committed":true}]
     */

    private String address;
    private String operator_address;
    private String cu_address;
    private String proposer_priority;
    private String voting_power;
    private String all_voting_power;
    private String voting_power_proportion;
    private String self_delegate_amount;
    private String self_delegate_proportion;
    private String other_delegate_amount;
    private String other_delegate_proportion;
    public  String apy;
    private String up_time;
    private boolean jailed;
    private int status;
    private int unbonding_height;
    private int unbonding_time;
    private CommissionBean commission;
    private DescriptionBean description;
    private List<LastBlocksBean> last_blocks;
    public boolean is_key_node;
    public boolean is_elected;
    /**
     * last_voted_time : 1584526635
     */

    private long last_voted_time;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOperator_address() {
        return operator_address;
    }

    public void setOperator_address(String operator_address) {
        this.operator_address = operator_address;
    }

    public String getCu_address() {
        return cu_address;
    }

    public void setCu_address(String cu_address) {
        this.cu_address = cu_address;
    }

    public String getProposer_priority() {
        return proposer_priority;
    }

    public void setProposer_priority(String proposer_priority) {
        this.proposer_priority = proposer_priority;
    }

    public String getVoting_power() {
        return voting_power;
    }

    public void setVoting_power(String voting_power) {
        this.voting_power = voting_power;
    }

    public String getAll_voting_power() {
        return all_voting_power;
    }

    public void setAll_voting_power(String all_voting_power) {
        this.all_voting_power = all_voting_power;
    }

    public String getVoting_power_proportion() {
        return voting_power_proportion;
    }

    public void setVoting_power_proportion(String voting_power_proportion) {
        this.voting_power_proportion = voting_power_proportion;
    }

    public String getSelf_delegate_amount() {
        return self_delegate_amount;
    }

    public void setSelf_delegate_amount(String self_delegate_amount) {
        this.self_delegate_amount = self_delegate_amount;
    }

    public String getSelf_delegate_proportion() {
        return self_delegate_proportion;
    }

    public void setSelf_delegate_proportion(String self_delegate_proportion) {
        this.self_delegate_proportion = self_delegate_proportion;
    }

    public String getOther_delegate_amount() {
        return other_delegate_amount;
    }

    public void setOther_delegate_amount(String other_delegate_amount) {
        this.other_delegate_amount = other_delegate_amount;
    }

    public String getOther_delegate_proportion() {
        return other_delegate_proportion;
    }

    public void setOther_delegate_proportion(String other_delegate_proportion) {
        this.other_delegate_proportion = other_delegate_proportion;
    }

    public String getUp_time() {
        return up_time;
    }

    public void setUp_time(String up_time) {
        this.up_time = up_time;
    }

    public boolean isJailed() {
        return jailed;
    }

    public void setJailed(boolean jailed) {
        this.jailed = jailed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUnbonding_height() {
        return unbonding_height;
    }

    public void setUnbonding_height(int unbonding_height) {
        this.unbonding_height = unbonding_height;
    }

    public int getUnbonding_time() {
        return unbonding_time;
    }

    public void setUnbonding_time(int unbonding_time) {
        this.unbonding_time = unbonding_time;
    }

    public CommissionBean getCommission() {
        return commission;
    }

    public void setCommission(CommissionBean commission) {
        this.commission = commission;
    }

    public DescriptionBean getDescription() {
        return description;
    }

    public void setDescription(DescriptionBean description) {
        this.description = description;
    }

    public List<LastBlocksBean> getLast_blocks() {
        return last_blocks;
    }

    public void setLast_blocks(List<LastBlocksBean> last_blocks) {
        this.last_blocks = last_blocks;
    }

    public long getLast_voted_time() {
        return last_voted_time;
    }

    public void setLast_voted_time(long last_voted_time) {
        this.last_voted_time = last_voted_time;
    }

    public static class CommissionBean {
        /**
         * rate : string
         * max_rate : string
         * max_change_rate : string
         */

        private String rate;
        private String max_rate;
        private String max_change_rate;

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getMax_rate() {
            return max_rate;
        }

        public void setMax_rate(String max_rate) {
            this.max_rate = max_rate;
        }

        public String getMax_change_rate() {
            return max_change_rate;
        }

        public void setMax_change_rate(String max_change_rate) {
            this.max_change_rate = max_change_rate;
        }
    }

    public static class DescriptionBean {
        /**
         * moniker : string
         * identity : string
         * website : string
         * details : string
         * avatar : string
         */

        private String moniker;
        private String identity;
        private String website;
        private String details;
        private String avatar;

        public String getMoniker() {
            return moniker;
        }

        public void setMoniker(String moniker) {
            this.moniker = moniker;
        }

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }

    public static class LastBlocksBean {
        /**
         * height : 0
         * committed : true
         */

        private int height;
        private boolean committed;

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public boolean isCommitted() {
            return committed;
        }

        public void setCommitted(boolean committed) {
            this.committed = committed;
        }
    }
}
