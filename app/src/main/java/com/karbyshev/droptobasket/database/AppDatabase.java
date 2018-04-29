package com.karbyshev.droptobasket.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.karbyshev.droptobasket.model.DroppedItem;
import com.karbyshev.droptobasket.model.Item;

@Database(entities = {Item.class, DroppedItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

        public abstract ProductsDao productsDao();
        public abstract DroppedProductsDao droppedProductsDao();
}
