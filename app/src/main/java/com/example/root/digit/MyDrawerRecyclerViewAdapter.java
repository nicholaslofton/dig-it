package com.example.root.digit;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by nglofton on 6/14/15.
 */
public class MyDrawerRecyclerViewAdapter extends RecyclerView.Adapter<MyDrawerRecyclerViewAdapter.ViewHolder> {

    private OnItemClickListener mItemClickListener;
    private List<Map<String, ?>> mDataset;
    private Context mContext;
    public int mCurrentPosition;

    public MyDrawerRecyclerViewAdapter(Context myContext, List<Map<String, ?>> myDataset) {
        mContext = myContext;
        mDataset = myDataset;
    }

    public interface onListItemSelectedListener {
        void onListItemSelected(int position);
    }

    @Override
    public MyDrawerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case DrawerData_New.TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
                break;
            case DrawerData_New.TYPE1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item1, parent, false);
                break;
            case DrawerData_New.TYPE2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item2, parent, false);
                break;
            case DrawerData_New.TYPE3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item3, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item1, parent, false);
                break;
        }

        ViewHolder viewHolder = new ViewHolder(view, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, ?> item = mDataset.get(position);
        holder.bindData(item, position);
    }

    @Override
    public int getItemViewType (int position) {
        Map<String, ?> item = mDataset.get(position);
        return (Integer) item.get("type");
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnClickListener (final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View vView;
        public int vViewType;
        public ImageView vIcon;
        public TextView vTitle;
        public ImageView vLine;
        public TextView vName;
        public ImageView vProfile;
        public TextView vEmail;

        public ViewHolder(View view, int viewtype) {
            super(view);
            vView = view;
            vViewType = viewtype;
            vIcon = (ImageView) view.findViewById(R.id.icon);
            vTitle = (TextView) view.findViewById(R.id.title);
            vName = (TextView) view.findViewById(R.id.name);
            vProfile = (ImageView) view.findViewById(R.id.circleView);
            vEmail = (TextView) view.findViewById(R.id.email);
            vLine = (ImageView) view.findViewById(R.id.line);

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, getPosition());
                    }

                    mCurrentPosition = getPosition();
                    notifyDataSetChanged();
                }
            });

        }

        public void bindData(Map<String, ?> item, int position) {

            if (vViewType != DrawerData_New.TYPE_HEADER) {
                if (position == mCurrentPosition) {
                    vView.setBackgroundColor(Color.LTGRAY);
                }
                else {
                    vView.setBackgroundColor(0x00000000);
                }
            }

            switch (vViewType) {
                case DrawerData_New.TYPE_HEADER:
                    if (vProfile != null) {
                        if (item.get("icon") == null ) {
                            vProfile.setImageResource(R.drawable.no_face_icon);
                        }
                        else {
                            String cleanImage = (item.get("icon") == null) ? null : (String) item.get("icon");
                            vProfile.setImageBitmap(UserFragment.StringToBitMap(cleanImage));
                        }
                    }
                    if (vName != null) {
                        vName.setText((String) item.get("username"));
                    }
                    if (vEmail != null) {
                        vEmail.setText((String) item.get("email"));
                    }
                    break;
                case DrawerData_New.TYPE1:
                    if (vIcon != null) {
                        vIcon.setImageResource(((Integer) item.get("icon")).intValue());
                    }
                    if (vTitle != null) {
                        vTitle.setText((String) item.get("title"));
                    }
                    break;
                case DrawerData_New.TYPE2:
                    if (vLine != null) {
                        vLine.setImageResource(((Integer) item.get("icon")).intValue());
                    }
                    break;
                case DrawerData_New.TYPE3:
                    if (vIcon != null) {
                        vIcon.setImageResource(((Integer) item.get("icon")).intValue());
                    }
                    if (vTitle != null) {
                        vTitle.setText((String) item.get("title"));
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
