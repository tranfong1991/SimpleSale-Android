package andytran.simplesale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Andy Tran on 6/20/2015.
 */
public class SaleItemAdapter extends ArrayAdapter<SaleItem> implements Filterable {
    private Context context;
    private Bitmap placeholder;
    private List<SaleItem> originalItems;
    private List<SaleItem> filteredItems;
    private SaleItemFilter saleItemFilter = new SaleItemFilter();

    public SaleItemAdapter(Context context, List<SaleItem> items) {
        super(context, -1, items);
        this.context = context;
        this.originalItems = items;
        this.filteredItems = items;
        this.placeholder = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_loading);
    }

    public int getCount() {
        return filteredItems.size();
    }

    public SaleItem getItem(int position) {
        return filteredItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_row, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.itemDescription = (TextView) rowView.findViewById(R.id.item_description);
            viewHolder.itemPrice = (TextView) rowView.findViewById(R.id.item_price);
            viewHolder.modeImage = (ImageView) rowView.findViewById(R.id.mode_image);
            viewHolder.itemImage = (CircleImageView) rowView.findViewById(R.id.item_image);

            rowView.setTag(viewHolder);
        }

        SaleItem item = filteredItems.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.itemDescription.setText(item.getDescription());
        holder.itemPrice.setText("$" + String.format("%.2f", item.getPrice()));

        if (!item.getImagePath().equals("DEFAULT")) {
            loadBitmap(item.getImagePath(), holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.img_no_image);
        }

        switch (item.getMode()) {
            case SaleItem.PRIVATE_MODE:
                holder.modeImage.setImageResource(R.drawable.ic_lock);
                break;
            case SaleItem.FRIEND_MODE:
                holder.modeImage.setImageResource(R.drawable.ic_friends);
                break;
            default:
                holder.modeImage.setImageResource(R.drawable.ic_globe);
                break;
        }

        return rowView;
    }

    public void loadBitmap(String imagePath, ImageView imageView) {
        if (Utils.cancelPotentialWork(imagePath, imageView)) {
            final MainActivity mainActivity = (MainActivity) context;
            final Bitmap bitmap = mainActivity.getBitmapFromMemCache(imagePath);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                final LoadImageAsyncTask task = new LoadImageAsyncTask(context, imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), placeholder, task);

                imageView.setImageDrawable(asyncDrawable);
                task.execute(imagePath);
            }
        }
    }

    public Filter getFilter() {
        return this.saleItemFilter;
    }

    static class ViewHolder {
        public TextView itemPrice;
        public TextView itemDescription;
        public CircleImageView itemImage;
        public ImageView modeImage;
    }

    private class SaleItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final List<SaleItem> originalList = originalItems;
            final List<SaleItem> newList = new ArrayList<SaleItem>();

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            for (int i = 0; i < originalList.size(); i++) {
                SaleItem item = originalList.get(i);

                if (item.getDescription().toLowerCase().contains(filterString)) {
                    newList.add(item);
                }
            }

            results.values = newList;
            results.count = newList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (ArrayList<SaleItem>) results.values;
            SaleItemAdapter.this.notifyDataSetChanged();
        }
    }
}
