/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.jboss.seam.example.booking {

    [Bindable]
    [RemoteClass(alias="org.jboss.seam.example.booking.Hotel")]
    public class Hotel extends HotelBase {
		
		public function get cityState():String {
			return city + ", " + state + ", " + country;
		}
    }
}