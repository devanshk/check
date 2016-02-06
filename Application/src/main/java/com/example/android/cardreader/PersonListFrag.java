package com.example.android.cardreader;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by dkukreja on 2/5/16.
 */
public class PersonListFrag extends Fragment {
    RecyclerView mRecyclerView;
    VerticalRecyclerViewFastScroller mScroller;
    ArrayList<User> mDataset;
    MyAdapter mAdapter;

    public PersonListFrag(ArrayList<User> data){
        mDataset = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_person_list, container, false);

        mRecyclerView = (RecyclerView)v.findViewById(R.id.recyclerView);
        mScroller = (VerticalRecyclerViewFastScroller)v.findViewById(R.id.fast_scroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        mScroller.setRecyclerView(mRecyclerView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.setOnScrollListener(mScroller.getOnScrollListener());

        //Set a Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<User> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View mView;
            public ViewHolder(View v) {
                super(v);
                mView = v;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<User> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
            // set the view's size, margins, paddings and layout parameters

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            User cur = mDataset.get(position);
            View v = holder.mView;
            TextView nameView = ((TextView)v.findViewById(R.id.name));
            TextView timeView = ((TextView) v.findViewById(R.id.time));

            nameView.setText(cur.name);
            v.findViewById(R.id.indicator).setBackgroundColor(getResources().getColor(R.color.gray));
            timeView.setText("");
            if (cur.checkinTime != null) {
                String minuteString = ""+cur.checkinTime.getMinutes();
                if (minuteString.length() == 1)
                    minuteString = "0"+minuteString;
                timeView.setText("" + cur.checkinTime.getHours() + ":" + minuteString);
                v.findViewById(R.id.indicator).setBackgroundColor(getResources().getColor(R.color.blue));
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
