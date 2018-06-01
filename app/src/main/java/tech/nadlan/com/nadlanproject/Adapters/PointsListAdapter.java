package tech.nadlan.com.nadlanproject.Adapters;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tech.nadlan.com.nadlanproject.R;
import tech.nadlan.com.nadlanproject.RentPoint;

/**
 * Created by מוחמד on 01/06/2018.
 */

public class PointsListAdapter extends BaseAdapter {
    Context context;
    List<RentPoint> pointList;


    public PointsListAdapter(Context context, List<RentPoint> pointList)
    {
        this.context = context;
        this.pointList = pointList;
    }



    @Override
    public int getCount() {
        return pointList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.point_item_layout, null);
            TextView titleEt = (TextView) convertView.findViewById(R.id.details_title_Tv);
            TextView descEt = (TextView) convertView.findViewById(R.id.details_desc_Tv);
            LinearLayout deleteLinear = (LinearLayout) convertView.findViewById(R.id.deleteLinear);
            ImageView pointImageView = (ImageView) convertView.findViewById(R.id.imageview);
            titleEt.setText(pointList.get(position).getType() + " " + pointList.get(position).getArea() +" מ\"ר" +", "+pointList.get(position).getEstablishYear());
            descEt.setText(pointList.get(position).getAddress() +", "+pointList.get(position).getCity());

            Picasso.with(context).load(pointList.get(position).getPhotoPath()).into(pointImageView);
        }



        return convertView;
    }
}
