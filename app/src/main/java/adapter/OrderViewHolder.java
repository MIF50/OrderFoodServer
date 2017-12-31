package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import common.Common;
import inter.ItemClickListener;
import mif50.com.orderfoodsserver.R;

/**
 * view holder of item of Order
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                        View.OnCreateContextMenuListener{
    // view in order_layout
    public TextView txt_id,txt_status,txt_phone,txt_address;
    // interface ItemclickListener
    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);
        // find View
        txt_id=itemView.findViewById(R.id.txt_id);
        txt_status=itemView.findViewById(R.id.txt_status);
        txt_phone=itemView.findViewById(R.id.txt_phone);
        txt_address=itemView.findViewById(R.id.txt_address);
        // action itemView
        itemView.setOnClickListener(this);
        //action Context menu
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        // what happen when click on itemView  => show action in ItemClickListener
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
    /*action of context menu listener (select Action,update,delete)*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("select Action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);
    }
}
