package com.ragab.ahmed.educational.movieapp.ui.detailscreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ragab.ahmed.educational.movieapp.R;
import com.ragab.ahmed.educational.movieapp.data.models.Review;

import java.util.List;

/**
 * Created by Ragabov on 12/18/2015.
 */
public class ReviewListViewAdapter extends ArrayAdapter<Review> {
    LayoutInflater minflater;

    public ReviewListViewAdapter(Context context, List<Review> reviews) {
        super(context, R.layout.trailer_list_item, reviews);
        minflater = LayoutInflater.from(getContext());
    }

    @Override public View getView(final int position, View convertView, ViewGroup parent) {

        Holder holder;
        if (convertView == null) {
            convertView =  minflater.inflate(R.layout.review_list_item, null);
            holder = new Holder();
            holder.authorText = (TextView) convertView.findViewById(R.id.review_author_txt);
            holder.contentText = (TextView) convertView.findViewById(R.id.review_content_txt);
            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder) convertView.getTag();
        }
        holder.authorText.setText(getItem(position).author);
        holder.contentText.setText(getItem(position).content);

        return convertView;
    }

    private class Holder{
        public TextView authorText;
        public TextView contentText;
    }

}
