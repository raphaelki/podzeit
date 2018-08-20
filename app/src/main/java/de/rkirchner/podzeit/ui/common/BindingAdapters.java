package de.rkirchner.podzeit.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;

public class BindingAdapters {

    @BindingAdapter(value = {"imageUrl", "requestListener"})
    public static void loadImage(ImageView view, String imageUrl, RequestListener<Drawable> listener) {
        if (imageUrl != null) {
            Glide.with(view)
                    .load(imageUrl)
                    .listener(listener)
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
