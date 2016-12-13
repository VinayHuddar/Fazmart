package com.fazmart.androidapp.Model.DeliveryData;

/**
 * Created by Vinay on 24-06-2015.
 */
public class PaymentMethods {
    PaymentMethod[] payment_methods;

    class PaymentMethod {
        String code;
        String title;
        String terms;
    }

    public String GetPaymentMethodCode (int methodIdx) { return payment_methods[methodIdx].code; }
    public String GetPaymentMethodName (int methodIdx) { return payment_methods[methodIdx].title; }
    public int GetPaymentMethodsCount () { return payment_methods.length; }
}
