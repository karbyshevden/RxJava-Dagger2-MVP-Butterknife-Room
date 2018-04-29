package com.karbyshev.droptobasket.activities;

import com.karbyshev.droptobasket.model.Item;

import java.util.List;

public interface IOnItemClickListener {

    void OnItemClick (int position, List<Item> list);
}
