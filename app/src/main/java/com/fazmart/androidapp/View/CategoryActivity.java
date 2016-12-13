package com.fazmart.androidapp.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fazmart.androidapp.Controller.Common.ImageDownloader;
import com.fazmart.androidapp.Model.CategoryData;
import com.fazmart.androidapp.View.Common.BaseActivity;
import com.fazmart.androidapp.Controller.Category.CategoryController;
import com.fazmart.androidapp.Common.Events.EventCategoriesFetched;
import com.fazmart.androidapp.Common.Events.EventGetCategories;
import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.Common.BreadCrumbAdapter;
import com.fazmart.androidapp.View.Common.BreadcrumbAdapterCallbacks;
import com.fazmart.androidapp.View.Common.NavDrawerItemList;
import com.fazmart.androidapp.View.Common.NavigationDrawerCallbacks;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CategoryActivity extends BaseActivity implements BreadcrumbAdapterCallbacks, NavigationDrawerCallbacks {
    static final int BREADCRUMB_NAV = 0;
    public static final String LINEAGE = "ANCESTOR_IDS";
    public static final String CATEGORY_ID = "PARENT_CATEGORY_ID";
    public static final String HIERARCHY_BACK_TRACK_CNT = "HIERARCHY_BACK_TRACK_CNT";

    private RecyclerView mCategoryList = null;

    CategoryController mCategoryController = null;
    int[] mLineage = null;
    CategoryActivity mActivity = null;

    ImageDownloader imageDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_store_landing_screen);

        super.onCreateDrawer();
        mNavigationDrawerFragment.SetDefaultSelectedPosition(NavDrawerItemList.BROWSE_ITEM_ID);

        getSupportActionBar().setTitle("Categories");

        mActivity = this;
        mLineage = getIntent().getIntArrayExtra(LINEAGE);

        imageDownloader = new ImageDownloader();

        mCategoryController = CategoryController.GetInstance();
        if (mCategoryController.IsCategoryTreeBuilt() == false) {
            findViewById(R.id.loading_message).setVisibility(View.VISIBLE);
            findViewById(R.id.CategoryViewContent).setVisibility(View.INVISIBLE);

            EventBus.getDefault().post(new EventGetCategories());
        } else {
            findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
            findViewById(R.id.CategoryViewContent).setVisibility(View.VISIBLE);
            ShowSubCategories();
        }
    }

    public void onEvent (EventCategoriesFetched evt) {
        findViewById(R.id.loading_message).setVisibility(View.INVISIBLE);
        findViewById(R.id.CategoryViewContent).setVisibility(View.VISIBLE);

        ShowSubCategories();
    }

    @Override
    public void onBreadcrumbItemSelected(int position) {
        if (position < mLineage.length-1) {
            Intent intent = new Intent();
            intent.putExtra(HIERARCHY_BACK_TRACK_CNT, (mLineage.length - 1) - position);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    void ShowSubCategories() {
        String[] catNames;
        final int hrchyLevel = mLineage.length;
        final int categoryId = mLineage[mLineage.length - 1];

        mCategoryList = (RecyclerView)findViewById(R.id.breadcrumb);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mCategoryList.setLayoutManager(layoutManager);

        final List<String> catNameList = new ArrayList<String>();
        final String rootCatName = "Main Sections";
        catNameList.add(rootCatName);

        if (categoryId == -1) {
            BreadCrumbAdapter adapter = new BreadCrumbAdapter(catNameList, this, false);
            adapter.setBreadcrumbAdapterCallbacks(this);
            mCategoryList.setAdapter(adapter);
        } else {
            String catName;
            for (int i = 1; i < hrchyLevel; i++) {
                catName = String.format(" > %s", mCategoryController.GetCategoryName(mLineage[i]));
                catNameList.add(catName);
            }

            BreadCrumbAdapter adapter = new BreadCrumbAdapter(catNameList, mActivity, false);
            adapter.setBreadcrumbAdapterCallbacks(mActivity);
            mCategoryList.setAdapter(adapter);

            layoutManager.scrollToPositionWithOffset(hrchyLevel - 1, 20);
        }

        // Setup GridView of Sub Categories
        final CategoryData.Category[] subCategories;
        CategoryData categoryData = CategoryData.GetInstance();
        if (categoryId == -1)
            subCategories = categoryData.GetMainCategories();
        else
            subCategories = categoryData.GetCategory(categoryId).GetSubCategories();

        GridView categoryGrid = (GridView) findViewById(R.id.CategoryGrid);
        categoryGrid.setAdapter(new AdapterGrocerySubCats(subCategories));
        categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View v,
                                    int position, long id) {
                int selectedCatId = subCategories[position].GetId();
                // Add the current category to the lineage and pass it to the child
                int[] extendedLineage = new int[mLineage.length + 1];
                for (int i = 0; i < mLineage.length; i++)
                    extendedLineage[i] = mLineage[i];
                extendedLineage[mLineage.length] = selectedCatId;

                if (subCategories[position].GetSubCategories().length > 0) { // Show next level categories
                    // Start a new CategoryActivity activity for the next level category
                    Intent intent = new Intent(mActivity, CategoryActivity.class);
                    intent.putExtra(CategoryActivity.LINEAGE, extendedLineage);
                    startActivityForResult(intent, BREADCRUMB_NAV);
                } else { // This is the leaf category. Bring up the product list view of this category
                    Intent intent = new Intent(mActivity, ProductListActivity.class);
                    intent.putExtra(CategoryActivity.LINEAGE, extendedLineage);
                    //intent.putExtra(CATEGORY_ID, categoryId);
                    startActivityForResult(intent, BREADCRUMB_NAV);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.pink_icon);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if ((requestCode == BREADCRUMB_NAV) && (resultCode == RESULT_OK) && (data != null)) {
            int hrchyBackTrack = data.getIntExtra(HIERARCHY_BACK_TRACK_CNT, 0);
            --hrchyBackTrack;
            if (hrchyBackTrack != 0) {
                Intent intent = new Intent();
                intent.putExtra(HIERARCHY_BACK_TRACK_CNT, hrchyBackTrack);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    public class AdapterGrocerySubCats extends BaseAdapter {
        CategoryData.Category[] mSubCategories;
        public AdapterGrocerySubCats(CategoryData.Category[] subCategories) {
            mSubCategories = subCategories;
        }

        public int getCount() {
            if (mSubCategories != null)
                return mSubCategories.length;
            else
                return 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            Context mContext = mActivity;
            View gridItem;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                gridItem = inflater.inflate(R.layout.sub_category_item, null);
            } else {
                gridItem = (View) convertView;
            }

            TextView textView = (TextView) gridItem.findViewById(R.id.SubCatName);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(mSubCategories[position].GetName());

            ImageView imageView = (ImageView) gridItem.findViewById(R.id.SubCatImage);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            String imageURL = mSubCategories[position].GetImageURL();
            if (imageURL != null)
                imageDownloader.download(imageURL, imageView);
            else
                imageView.setImageResource(R.drawable.store_logo);

            //gridItem.setLayoutParams(new GridView.LayoutParams(200, 250));
            return gridItem;
        }
    }


    @Override
    public void onResume () {
        super.onResume();

        /*if (findViewById(R.id.breadcrumb).isInLayout() == false) {
            HorizontalListView breadcrumb = (HorizontalListView) findViewById(R.id.breadcrumb);
            breadcrumb.scrollTo(mScrollToLen);
        }*/

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause () {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }

    @Override
    public int getDefaultItemSelectId() {
        return NavDrawerItemList.BROWSE_ITEM_ID;
    }
}

