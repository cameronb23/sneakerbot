package me.cameronb.bot.credentials;

import lombok.Getter;
import me.cameronb.bot.util.HtmlFieldId;

/**
 * Created by Cameron on 5/22/2017.
 */
public class BillingProfile {

    @Getter @HtmlFieldId("dwfrm_payment_creditCard_owner")
    private String nameOnCard;

    @Getter @HtmlFieldId("dwfrm_payment_creditCard_number")
    private String cardNumber;

    @Getter @HtmlFieldId("dwfrm_payment_creditCard_month")
    private String month; // must have 0 before if not >= 10

    @Getter @HtmlFieldId("dwfrm_payment_creditCard_year")
    private String year; // FULL YEAR..

}
