package com.ragab.ahmed.educational.movieapp.ui.detailscreen;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ragab.ahmed.educational.movieapp.R;
import com.ragab.ahmed.educational.movieapp.data.models.Trailer;
import com.ragab.ahmed.educational.movieapp.network.Constants;

import java.util.List;

/**
 * Created by Ragabov on 12/18/2015.
 */
public class TrailerListViewAdapter extends ArrayAdapter<Trailer> {
    LayoutInflater minflater;

    public TrailerListViewAdapter(Context context, List<Trailer> trailers) {
        super(context, R.layout.trailer_list_item, trailers);
        minflater = LayoutInflater.from(getContext());
    }

    @Override public View getView(final int position, View convertView, ViewGroup parent) {

        Holder holder;
        if (convertView == null) {
            convertView =  minflater.inflate(R.layout.trailer_list_item, null);
            holder = new Holder();
            holder.textView = (TextView) convertView.findViewById(R.id.trailer_name_txt);
            holder.imageButton = (ImageButton) convertView.findViewById(R.id.play_trailer_btn);
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("vnd.youtube:" + getItem(position).key));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }catch (ActivityNotFoundException ex){
                        Intent intent=new Intent(Intent.ACTION_VIEW,
                                Uri.parse(Constants.YOUTUBE_VIDEO_BASE_URL+ "?v=" + getItem(position).key));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                }
            });
            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder) convertView.getTag();
        }
        holder.textView.setText(getItem(position).name);

        return convertView;
    }

    private class Holder{
        public TextView textView;
        public ImageButton imageButton;
    }

}
