package com.fazmart.androidapp.View.Checkout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fazmart.androidapp.Model.OrderSummaryData;
import com.fazmart.androidapp.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrderSummaryActivity_SummaryFragment extends Fragment {
    private static final int DIALOG_APPLY_COUPON = 1;
    private static final int DIALOG_USE_GIFT_VOUCHER = 2;
    private static final int DIALOG_REDEEM_POINTS = 3;

    private static OrderSummaryActivity_SummaryFragment instance = null;
    public static OrderSummaryActivity_SummaryFragment GetInstance () {
        if (instance == null)
            instance = new OrderSummaryActivity_SummaryFragment();
        return instance;
    }

    public OrderSummaryActivity_SummaryFragment() {
    }

    void ShowPriceSummary () {
        TextView subtotalLabelTV = (TextView)mRootView.findViewById(R.id.order_summary_subtotal_label);
        subtotalLabelTV.setText(mOrderSummaryData.GetSubTotalLabel());
        TextView subtotalTV = (TextView)mRootView.findViewById(R.id.order_summary_subtotal);
        subtotalTV.setText(mOrderSummaryData.GetSubTotal());

        TextView delChargesLabelTV = (TextView)mRootView.findViewById(R.id.order_summary_delivery_charges_label);
        delChargesLabelTV.setText(mOrderSummaryData.GetDeliveryChargesLabel());
        TextView delChargesTV = (TextView)mRootView.findViewById(R.id.order_summary_delivery_charges);
        delChargesTV.setText(mOrderSummaryData.GetDeliveryCharges());

        TextView applyCouponTV = (TextView) mRootView.findViewById(R.id.apply_coupon);
        applyCouponTV.setPaintFlags(applyCouponTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        applyCouponTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_APPLY_COUPON);
            }
        });

        TextView useGiftVoucherTV = (TextView) mRootView.findViewById(R.id.use_gift_voucher);
        useGiftVoucherTV.setPaintFlags(useGiftVoucherTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        useGiftVoucherTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_USE_GIFT_VOUCHER);
            }
        });

        TextView redeemPointsTV = (TextView) mRootView.findViewById(R.id.redeem_points);
        redeemPointsTV.setPaintFlags(redeemPointsTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        redeemPointsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_REDEEM_POINTS);
            }
        });

        TextView totalLabelTV = (TextView)mRootView.findViewById(R.id.order_summary_total_label);
        totalLabelTV.setText(mOrderSummaryData.GetTotalLabel());
        TextView totalTV = (TextView)mRootView.findViewById(R.id.order_summary_total);
        totalTV.setText(mOrderSummaryData.GetTotal());
    }

    void ShowDeliveryInfo () {
        TextView nameTV = (TextView)mRootView.findViewById(R.id.username);
        nameTV.setText(String.format("%s %s", mOrderSummaryData.GetFirstName(), mOrderSummaryData.GetLastName()));

        TextView addressTV = (TextView)mRootView.findViewById(R.id.user_address);
        String address = String.format("%s\n%s\n%s\n%s - %s",mOrderSummaryData.GetApartmentNumber(),
                mOrderSummaryData.GetApartmentName(), mOrderSummaryData.GetAreaName(),
                mOrderSummaryData.GetCityName(), mOrderSummaryData.GetPostcode());
        addressTV.setText(address);

        TextView mobileTV = (TextView)mRootView.findViewById(R.id.mobile);
        mobileTV.setText(((OrderSummaryActivity)getActivity()).GetMobileNumber()); //UserAccountData.GetInstance().GetAccountData().GetMobileNumber());

        TextView emailTV = (TextView)mRootView.findViewById(R.id.email);
        emailTV.setText(((OrderSummaryActivity)getActivity()).GetEmailId()); //UserAccountData.GetInstance().GetAccountData().GetEmail());

        //android:text="Tomorrow, 10AM to 11AM"
        TextView delTime = (TextView)mRootView.findViewById(R.id.delivery_time_summary);
        delTime.setText(mOrderSummaryData.GetDeliverySlot());
    }

    OrderSummaryData mOrderSummaryData = null;
    View mRootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_ordersummary_order_summary, container, false);
        //mOrderSummaryData = ReceivedOrderSummaryData.GetInstance().GetOrderSummaryData();
        mOrderSummaryData = ((OrderSummaryActivity)getActivity()).GetOrderSummaryData();

                ShowPriceSummary();
        ShowDeliveryInfo();
        // Show payment method
        TextView payMethod = (TextView)mRootView.findViewById(R.id.payment_method);
        payMethod.setText(mOrderSummaryData.GetPaymentMethod());

        return mRootView;
    }

    void showDialog(int dialogType) {
        DialogFragment newFragment = RewardsDialogFragment.newInstance(dialogType);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    public static class RewardsDialogFragment extends DialogFragment {
        public static RewardsDialogFragment newInstance(int dialogType) {
            RewardsDialogFragment frag = new RewardsDialogFragment();
            Bundle args = new Bundle();
            args.putInt("DIALOG_TYPE", dialogType);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int dialogType = getArguments().getInt("DIALOG_TYPE");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    //.setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    doNegativeClick();
                                }
                            }
                    );

            LayoutInflater inflater = getActivity().getLayoutInflater();
            EditText editText = null;
            switch(dialogType) {
                case DIALOG_APPLY_COUPON:
                    builder.setTitle(R.string.apply_coupon_label);
                    builder.setView(inflater.inflate(R.layout.apply_coupon_dialog_layout, null, false));
                    break;
                case DIALOG_USE_GIFT_VOUCHER:
                    builder.setTitle(R.string.use_gift_voucher_label);
                    builder.setView(inflater.inflate(R.layout.use_gift_voucher_dialog_layout, null, false));
                    break;
                case DIALOG_REDEEM_POINTS:
                    builder.setView(inflater.inflate(R.layout.redeem_points_dialog_layout, null, false));
                    builder.setTitle(R.string.redeem_points_label);
                    break;
            }

            return  builder.create();
        }

        public void doPositiveClick() {
            // Do stuff here.
            Log.i("FragmentAlertDialog", "Positive click!");
        }

        public void doNegativeClick() {
            // Do stuff here.
            Log.i("FragmentAlertDialog", "Negative click!");
        }
    }


    /*protected Dialog onCreateDialog(int id) {
        switch (id) {
            case (DIALOG_APPLY_COUPON):
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View applyCouponView = factory.inflate(R.layout.apply_coupon_dialog_layout, null);
                return new AlertDialog.Builder(getActivity())
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(R.string.apply_coupon_label)
                        .setView(applyCouponView)
                        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
            case (DIALOG_USE_GIFT_VOUCHER):
                factory = LayoutInflater.from(getActivity());
                final View useGiftVoucherView = factory.inflate(R.layout.use_gift_voucher_dialog_layout, null);
                return new AlertDialog.Builder(getActivity())
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(R.string.use_gift_voucher_label)
                        .setView(useGiftVoucherView)
                        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .create();
        }
    }*/
}

