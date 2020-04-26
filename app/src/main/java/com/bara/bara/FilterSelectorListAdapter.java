package com.bara.bara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.bara.bara.FilterSelectorList.OnListFragmentInteractionListener;
import com.bara.bara.CameraFilterProvider.FilterSelectorItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FilterSelectorListAdapter extends RecyclerView.Adapter<FilterSelectorListAdapter.ViewHolder> {

    private final List<FilterSelectorItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private ImageButton mSelectedButton;

    public FilterSelectorListAdapter(List<FilterSelectorItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_filter_selector_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setImageResource(mValues.get(position).image);
        if (mValues.get(position).image == R.drawable.horse)
        {
            mSelectedButton = holder.mContentView;
            holder.mContentView.setSelected(true);
        }

        holder.mContentView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
                if (mSelectedButton != null)
                {
                    mSelectedButton.setSelected(false);
                }
                mSelectedButton = holder.mContentView;
                holder.mContentView.setSelected(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageButton mContentView;
        public FilterSelectorItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (ImageButton) view.findViewById(R.id.imageName);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getId() + "'";
        }
    }
}
