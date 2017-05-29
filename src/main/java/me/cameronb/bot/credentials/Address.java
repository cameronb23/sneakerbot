package me.cameronb.bot.credentials;

import lombok.Getter;
import me.cameronb.bot.util.HtmlFieldId;

/**
 * Created by Cameron on 5/22/2017.
 */
public class Address {

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_address1")
    private String address;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_address2")
    private String aptNumber;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_city")
    private String town;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_zip")
    private int zipCode;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_countyProvince")
    private State state;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_country")
    private String country = "US"; // US is the value in html form

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_phone")
    private String phone;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_email_emailAddress")
    private String email;

    @Getter @HtmlFieldId("dwfrm_cart_shippingMethodID_0")
    private String shippingMethod = "2ndDay"; // Standard only other option




    // SUBMIT REQUEST WITH:
    // "referer": "Cart-Show"
    // "shippingMethodType_0": "inline"
    // "dwfrm_cart_selectShippingMethod": "ShippingMethodID"
    // "dwfrm_delivery_singleshipping_shippingAddress_useAsBillingAddress": true


}
