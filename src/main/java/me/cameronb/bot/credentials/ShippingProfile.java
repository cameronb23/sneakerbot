package me.cameronb.bot.credentials;

import lombok.Getter;
import me.cameronb.bot.util.HtmlFieldId;

/**
 * Created by Cameron on 5/22/2017.
 */
public class ShippingProfile {

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_firstName")
    private String firstName;

    @Getter @HtmlFieldId("dwfrm_delivery_singleshipping_shippingAddress_addressFields_lastName")
    private String lastName;

    @Getter
    private Address address;

}
