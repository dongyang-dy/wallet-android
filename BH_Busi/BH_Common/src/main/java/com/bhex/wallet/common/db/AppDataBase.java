package com.bhex.wallet.common.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.bhex.wallet.common.db.dao.BHAddressBookDao;
import com.bhex.wallet.common.db.dao.BHTokenDao;
import com.bhex.wallet.common.db.dao.BHUserTokenDao;
import com.bhex.wallet.common.db.dao.BHWalletDao;
import com.bhex.wallet.common.db.entity.BHAddressBook;
import com.bhex.wallet.common.db.entity.BHUserToken;
import com.bhex.wallet.common.db.entity.BHWallet;
import com.bhex.wallet.common.model.BHToken;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/3/4
 * Time: 22:22
 */
@Database(entities = {BHWallet.class, BHToken.class, BHUserToken.class,BHAddressBook.class} , version = 5,exportSchema = false)
public abstract  class AppDataBase extends RoomDatabase {

    private static final String DB_NAME ="bh_db";


    private static volatile AppDataBase INSTANCE;

    public abstract BHWalletDao bhWalletDao();
    public abstract BHTokenDao  bhTokenDao();
    public abstract BHAddressBookDao bhAddressBookDao();
    public abstract BHUserTokenDao bhUserTokenDao();

    public static AppDataBase getInstance(Application context){
        if(INSTANCE==null){
            synchronized (AppDataBase.class){
                if(INSTANCE==null){
                    INSTANCE = buildDatabase(context);
                }
            }
        }
        return INSTANCE;
    }

    private static AppDataBase buildDatabase(Application context) {
        return Room.databaseBuilder(context,AppDataBase.class,DB_NAME).addCallback(new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }
        })
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .build();
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `tab_token` (`p_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `symbol` TEXT, `issuer` TEXT, `chain` TEXT, `type` INTEGER NOT NULL, `is_send_enabled` INTEGER NOT NULL, `is_deposit_enabled` INTEGER NOT NULL, `is_withdrawal_enabled` INTEGER NOT NULL, `decimals` INTEGER NOT NULL, `total_supply` TEXT, `collect_threshold` TEXT, `deposit_threshold` TEXT, `open_fee` TEXT,`collect_fee` TEXT, `sys_open_fee` TEXT, `withdrawal_fee` TEXT, `max_op_cu_number` INTEGER DEFAULT 0, `systransfer_amount` TEXT, `op_cu_systransfer_amount` TEXT, `is_native` INTEGER NOT NULL, `custodian_amount` TEXT, `logo` TEXT);");
            database.execSQL("CREATE UNIQUE INDEX `index_tab_token_symbol` ON `tab_token` (`symbol`);");
            database.execSQL("CREATE TABLE IF NOT EXISTS `tab_user_token` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `address` TEXT, `symbol` TEXT)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(" CREATE TABLE `tab_wallet_bak` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `address` TEXT, `name` TEXT, `keystorePath` TEXT, `isBackup` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL, `publicKey` TEXT, `way` INTEGER NOT NULL);");
            database.execSQL("insert into `tab_wallet_bak` (address,name,keystorePath,isBackup,isDefault,publicKey,way) \n" +
                    "\n" +
                    "select address,name,keystorePath,isBackup,isDefault,publicKey,way from `tab_wallet`");
            database.execSQL("drop table tab_wallet ");
            database.execSQL("alter table `tab_wallet_bak` rename to  tab_wallet ");

        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `tab_address_book` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `wallet_address` TEXT, `address` TEXT, `chain` TEXT, `symbol` TEXT, `address_name` TEXT, `address_remark` TEXT);");
        }
    };
}
