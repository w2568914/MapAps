package com.example.mapaps.adapter;

import com.amap.api.services.help.Tip;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;

public class DiffTipCall extends DiffUtil.Callback {
    private List<Tip> oldData;
    private List<Tip> newData;

    public DiffTipCall(List<Tip> old,List<Tip> data){
        this.oldData=old;
        this.newData=data;
    }

    @Override
    public int getOldListSize() {
        return oldData!=null? oldData.size():0;
    }

    @Override
    public int getNewListSize() {
        return newData!=null? newData.size():0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldData.get(oldItemPosition).getName().equals(newData.get(newItemPosition).getName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldData.get(oldItemPosition).getName().equals(newData.get(newItemPosition).getName());
    }
}
