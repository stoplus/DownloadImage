package com.example.den.downloadimage.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.den.downloadimage.R;
import com.example.den.downloadimage.entity.ImageObj;
import com.example.den.downloadimage.ui.MainActivity;
import com.example.den.downloadimage.utils.GlideApp;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdapterImage extends RecyclerView.Adapter<AdapterImage.ViewHolder> {

    private LayoutInflater inflater;
    private ImageObj[] list;
    private Context context;


    public AdapterImage(Context context, ImageObj[] list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
    }//AdapterForAdmin


    @Override
    public int getItemCount() {
        return list.length;
    }//getItemCount

    @Override
    public long getItemId(int position) {
        return position;
    }//getItemId

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_recycler, parent, false);
        return new ViewHolder(view);
    } // onCreateViewHolder


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.cardView)
        CardView cardView;

        private ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }//ViewHolder
    }//class ViewHolder


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Uri uri;
        String finalPath = list[position].getLinkDevice();
        uri = Uri.parse(finalPath);
        setPhoto(uri, holder, finalPath);
    }//onBindViewHolder


    private void setPhoto(Uri uri, ViewHolder holder, String finalPath) {
        GlideApp.with(context)
                .load(uri)
                .override(500,500)
                .fitCenter()
                .error(R.mipmap.default_photo)
                .listener(GlidePalette.with(finalPath)
                        .intoCallBack(new GlidePalette.CallBack() {

                            @Override
                            public void onPaletteLoaded(Palette palette) {
                                onPalette(palette);
                            }

                            private void onPalette(Palette palette) {
                                if (null != palette) {
                                    holder.cardView.setCardBackgroundColor(palette.getDarkVibrantColor(Color.GRAY));
                                }
                            }
                        }))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.imageView);
    }//setPhoto
}//class Adapter