package com.bhex.wallet.common.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.bhex.wallet.common.db.entity.BHAddressBook;

import java.util.List;

/**
 * @author gongdongyang
 * 2021-3-20 14:48:34
 */
@Dao
public interface BHAddressBookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<BHAddressBook> addressBooks);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertSingle(BHAddressBook addressBook);

    @Query("delete from tab_address_book where id=:p_id")
    void deleteAddress(long p_id);

    @Query("SELECT * FROM tab_address_book where wallet_address =:p_wallet_address and chain =:p_chain")
    List<BHAddressBook> loadAddressBook(String p_wallet_address,String p_chain);

    @Query("SELECT * FROM tab_address_book")
    List<BHAddressBook> loadAddressBook();

    @Query("SELECT * FROM tab_address_book where address=:p_address and chain=:p_chain")
    BHAddressBook existsAddress(String p_address,String p_chain );
}
