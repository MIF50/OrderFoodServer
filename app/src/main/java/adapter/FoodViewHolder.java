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
 * this adapter used to add and load food list of menu
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
                                                                        ,View.OnCreateContextMenuListener{
    public ImageView image_food_list;
    public TextView name_food_list;
    private ItemClickListener itemClickListener;
    public FoodViewHolder(View itemView) {
        super(itemView);
        image_food_list=itemView.findViewById(R.id.image_food_list);
        name_food_list=itemView.findViewById(R.id.txt_name_food_list);
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
    /* this method show what happen when click food list of menu show me dialog contain(select Action,update,delete)*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("select Action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);

    }
}
