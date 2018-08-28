package de.rkirchner.podzeit.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class BindingAdapters {

    @BindingAdapter(value = {"imageUrl", "requestListener"})
    public static void loadImage(ImageView view, String imageUrl, GlideRequestListener listener) {
        if (imageUrl != null) {
            Glide.with(view)
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            listener.onLoadFinished();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            listener.onLoadFinished();
                            return false;
                        }
                    })
                    .into(view);
        }
    }

    @BindingAdapter(value = {"imageUrl", "requestListener", "placeholder"})
    public static void loadImage(ImageView view, String imageUrl, GlideRequestListener listener, @DrawableRes int placeholder) {
        RequestOptions requestOptions = new RequestOptions().placeholder(placeholder);
        if (imageUrl != null) {
            Glide.with(view)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            listener.onLoadFinished();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            listener.onLoadFinished();
                            return false;
                        }
                    })
                    .into(view);
        }
    }

    @BindingAdapter(value = {"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl != null) {
            Glide.with(view)
                    .load(imageUrl)
                    .into(view);
        }
    }

    @BindingAdapter(value = {"visible", "dontRemoveFromLayout"}, requireAll = false)
    public static void setVisibility(View view, boolean visible, boolean dontRemoveFromLayout) {
        if (visible) view.setVisibility(View.VISIBLE);
        else if (!visible && dontRemoveFromLayout) view.setVisibility(View.INVISIBLE);
        else view.setVisibility(View.GONE);
    }
}
