package com.yjz.imageSelector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yjz.imageSelector.ImageSelector;
import com.yjz.imageSelector.R;
import com.yjz.imageSelector.bean.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter
 *
 * @author lizheng
 *         created at 2018/9/19 上午9:42
 */
public class ImageRecyAdapter extends RecyclerView.Adapter<ImageRecyAdapter.ViewHolder> {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean showCamera = true;
    private List<Image> mImages;
    private List<Image> mSelectedImages = new ArrayList<>();
    private int maxCount;
    private boolean isSingleMode;
    private OnImageItemClickListener listener;

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(Image imageItem, int position);

        void onCameraItemClick();
    }


    public ImageRecyAdapter(Context context, boolean isSingleMode, boolean showCamera, int maxCount) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.showCamera = showCamera;
        this.maxCount = maxCount;
        this.isSingleMode = isSingleMode;
        this.mImages = new ArrayList<>();
    }


    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<Image> images) {
        if (images != null && images.size() > 0) {
            mImages = images;
        } else {
            mImages = new ArrayList<>();
        }
        notifyDataSetChanged();
    }


    /**
     * 通过图片路径设置默认选择
     *
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        mSelectedImages.clear();
        for (String path : resultList) {
            Image image = getImageByPath(path);
            if (image != null) {
                mSelectedImages.add(image);
            }
        }
    }

    private Image getImageByPath(String path) {
        if (mImages != null && mImages.size() > 0) {
            for (Image image : mImages) {
                if (image.path.equalsIgnoreCase(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CAMERA) {
            return new ViewHolder(mInflater.inflate(R.layout.adapter_item_camera, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.adapter_item_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            holder.ivCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onCameraItemClick();
                    }
                }
            });
        } else if (getItemViewType(position) == TYPE_NORMAL) {
            final Image image = getItem(position);
            if (image == null) {
                return;
            }

            //改变选择状态
            setSelectStatus(holder, mSelectedImages.contains(image));
            // 显示图片
            ImageSelector.getInstance().getImageLoader().displayImage(mContext, image.path, holder.ivImage);

            holder.ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedImage(holder, image, position);
                }
            });
        }
    }

    /**
     * 设置选中状态
     */
    private void setSelectStatus(ViewHolder holder, boolean isSelected) {
        //单选状态不显示钩子
        if (isSingleMode) {
            holder.ivIndicator.setVisibility(View.GONE);
            holder.mask.setVisibility(View.GONE);
            return;
        }
        if (isSelected) {
            holder.ivIndicator.setVisibility(View.VISIBLE);
            holder.mask.setVisibility(View.VISIBLE);
        } else {
            holder.ivIndicator.setVisibility(View.GONE);
            holder.mask.setVisibility(View.GONE);
        }
    }

    private void selectedImage(ViewHolder holder, Image image, int position) {
        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
            setSelectStatus(holder, false);
        } else if (mSelectedImages.size() < maxCount) {
            mSelectedImages.add(image);
            setSelectStatus(holder, true);
        } else if (isSingleMode) {

        }
        if (listener != null) {
            listener.onImageItemClick(image, position);
        }
    }

    @Override
    public int getItemCount() {
        return showCamera ? mImages.size() + 1 : mImages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Image getItem(int position) {
        if (showCamera) {
            if (position == 0) {
                return null;
            }
            return mImages.get(position - 1);
        } else {
            return mImages.get(position);
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivIndicator;
        View mask;
        ImageView ivCamera;
        View itemView;


        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivImage = itemView.findViewById(R.id.image);
            ivIndicator = itemView.findViewById(R.id.checkmark);
            mask = itemView.findViewById(R.id.mask);
            ivCamera = itemView.findViewById(R.id.iv_camera);
        }
    }
}
