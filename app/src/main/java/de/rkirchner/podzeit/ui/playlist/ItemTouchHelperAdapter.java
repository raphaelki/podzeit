package de.rkirchner.podzeit.ui.playlist;

public interface ItemTouchHelperAdapter {

    void onItemDismiss(int position);

    boolean onItemMove(int startPosition, int endPosition);

    void onItemChangedPosition(int startPosition, int endPosition);
}
