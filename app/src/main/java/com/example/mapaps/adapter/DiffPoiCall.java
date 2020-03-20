package com.example.mapaps.adapter;

import com.amap.api.services.core.PoiItem;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;

public class DiffPoiCall extends DiffUtil.Callback {
    List<PoiItem> oldData;
    List<PoiItem> newData;

    public DiffPoiCall(List<PoiItem> old,List<PoiItem> data){
        this.oldData=old;
        this.newData=data;
    }

    @Override
    public int getOldListSize() {
        return oldData!=null?oldData.size():0;
    }

    @Override
    public int getNewListSize() {
        return newData!=null?newData.size():0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldData.get(oldItemPosition).getTitle().equals(newData.get(newItemPosition).getTitle());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldData.get(oldItemPosition).getTitle().equals(newData.get(newItemPosition).getTitle());
    }
}
