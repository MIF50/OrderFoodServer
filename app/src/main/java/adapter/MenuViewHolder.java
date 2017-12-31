package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import common.Common;
import inter.ItemClickListener;
import mif50.com.orderfoodsserver.R;

/**
 * Created by mohamed on 12/5/17.
 */

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                  View.OnCreateContextMenuListener{
    public ImageView image_item;
    public TextView txt_name_item;
    private ItemClickListener itemClickListener;
    public MenuViewHolder(View itemView) {
        super(itemView);
        image_item=itemView.findViewById(R.id.image_item);
        txt_name_item=itemView.findViewById(R.id.txt_name_item);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("select Action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);
    }
}
