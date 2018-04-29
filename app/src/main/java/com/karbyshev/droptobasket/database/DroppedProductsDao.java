package com.karbyshev.droptobasket.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.karbyshev.droptobasket.model.DroppedItem;
import com.karbyshev.droptobasket.model.Item;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface DroppedProductsDao {

    @Query("SELECT * FROM dropped_item")
    Single<List<DroppedItem>> getAll();

    @Query("DELETE FROM dropped_item")
    void clearTable();

    @Insert
    void insertAll(List<Item> list);

    @Insert
    void insert(DroppedItem... droppedItems);

    @Delete
    void deleteItem(DroppedItem... droppedItems);

    @Update
    void update(DroppedItem... droppedItems);
}
