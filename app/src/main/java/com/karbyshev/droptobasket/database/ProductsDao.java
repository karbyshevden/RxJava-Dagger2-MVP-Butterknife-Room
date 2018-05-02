package com.karbyshev.droptobasket.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.karbyshev.droptobasket.model.Item;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface ProductsDao {

    @Query("SELECT * FROM item")
    Flowable<List<Item>> getAll();

    @Query("DELETE FROM item")
    void clearTable();

    @Insert
    void insert(Item... items);

    @Delete
    void deleteItem(Item... item);

    @Update
    void update(Item... item);
}
