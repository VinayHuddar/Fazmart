package com.fazmart.androidapp.Controller.Category;

import com.fazmart.androidapp.Common.CommonDefinitions;
import com.fazmart.androidapp.Common.Events.EventCategoriesFetched;
import com.fazmart.androidapp.Common.Events.EventGetCategories;
import com.fazmart.androidapp.Controller.ProductList.ProductListManager;
import com.fazmart.androidapp.FazmartApplication;
import com.fazmart.androidapp.Model.CategoryData;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CategoryController {
    // Implement Singleton
    private static CategoryController instance = null;
    public static CategoryController GetInstance()
    {
        if(instance == null) {
            instance = new CategoryController();
        }
        return instance;
    }

    CategoryData mCategoryData;
    CategoryData.Category mRootCategory = null;

    public CategoryController() {
        mCategoryData = CategoryData.GetInstance();
        EventBus.getDefault().register(this);
    }

    public void onEvent (EventGetCategories evt) {
        // Fetch Category Data from the server if it is not already fetched
        if (mCategoryData.GetRootCategory() == null)
            GetCategoryData();
    }

    int retryCount = 0;
    public void GetCategoryData() {
        FazmartApplication.GetAPIService().GetCategoryTree(new Callback<CategoryData.MainCategories>() {
            @Override
            public void success(CategoryData.MainCategories mainCategories, Response response) {
                mCategoryData.BuildCategoryTree(mainCategories);
                mRootCategory = mCategoryData.GetRootCategory();
                EventBus.getDefault().post(new EventCategoriesFetched());
                retryCount = 0;
            }

            @Override
            public void failure(RetrofitError error) {
                // Attempt five retries before giving up
                retryCount++;
                if (retryCount < CommonDefinitions.RETRY_COUNT)
                    GetCategoryData();
                else {
                    // Show a "Network Problem" message to user and take him to landing screen
                    retryCount = 0;
                        /*Toast.makeText(mActivity, "There seems to be some problem with the network - " +
                                "either you are not connected to the network or the network is too slow or ",
                                Toast.LENGTH_SHORT);
                        Intent intent = new Intent(mActivity, IntegratedServicesScreen.class);
                        mActivity.startActivity(intent);*/
                }
            }
        });
    }

    public boolean WasCategoryViewed (int catId) {
        return ProductListManager.GetInstance().WasCategoryViewed(catId);
    }

    public boolean IsCategoryTreeBuilt() {
        return (mRootCategory != null);
    }

    public int GetMainCategoriesCount() {
        return (mRootCategory.GetSubCategories().length);
    }

    public CategoryData.Category GetMainCategory (int catIdx) {
        return mRootCategory.GetSubCategories()[catIdx];
    }

    public String GetCategoryName (int catId) {
        return mCategoryData.GetCategory(catId).GetName();
    }

    public int GetCategoryId (int catId) {
        return mCategoryData.GetCategory(catId).GetId();
    }
}
