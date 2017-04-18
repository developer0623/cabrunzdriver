/**
 * 
 */
package com.cabrunzltd.cabrunz.driver.model;

import java.io.Serializable;

/**
 * @author Hardik A Bhalodi
 * 
 */
public class Bill implements Serializable {
	private String distance;
	private String time;
	private String basePrice;
	private String distanceCost;
	private String timeCost;
	private String total;
	private String isPaid;
	private String unit;
	private String currency;
	private String quotedPrice;
	private String perdistancecost;
	private String pertimecost;
	private String promo, promo_discount, actual_price;

	private String primary_id;
	private String primary_amount;
	private String secoundry_id;
	private String secoundry_amount;
	private String payment_mode;

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(String basePrice) {
		this.basePrice = basePrice;
	}

	public String getDistanceCost() {
		return distanceCost;
	}

	public void setDistanceCost(String distanceCost) {
		this.distanceCost = distanceCost;
	}

	public String getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(String timeCost) {
		this.timeCost = timeCost;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(String isPaid) {
		this.isPaid = isPaid;
	}

	public String getPrimary_id() {
		return primary_id;
	}

	public void setPrimary_id(String primary_id) {
		this.primary_id = primary_id;
	}

	public String getPrimary_amount() {
		return primary_amount;
	}

	public void setPrimary_amount(String primary_amount) {
		this.primary_amount = primary_amount;
	}

	public String getSecoundry_id() {
		return secoundry_id;
	}

	public void setSecoundry_id(String secoundry_id) {
		this.secoundry_id = secoundry_id;
	}

	public String getSecoundry_amount() {
		return secoundry_amount;
	}

	public void setSecoundry_amount(String secoundry_amount) {
		this.secoundry_amount = secoundry_amount;
	}

	public String getPayment_mode() {
		return payment_mode;
	}

	public void setPayment_mode(String payment_mode) {
		this.payment_mode = payment_mode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getQuotedPrice() {
		return this.quotedPrice;
	}

	public void setQuotedPrice(String quotedPrice) {
		this.quotedPrice = quotedPrice;
	}

	public String getPerdistancecost() {
		return perdistancecost;
	}

	public void setPerdistancecost(String perdistancecost) {
		this.perdistancecost = perdistancecost;
	}

	public String getPertimecost() {
		return pertimecost;
	}

	public void setPertimecost(String pertimecost) {
		this.pertimecost = pertimecost;
	}

	public String getPromo() {
		return promo;
	}

	public void setPromo(String promo) {
		this.promo = promo;
	}

	public String getPromo_discount() {
		return promo_discount;
	}

	public void setPromo_discount(String promo_discount) {
		this.promo_discount = promo_discount;
	}

	public String getActual_price() {
		return actual_price;
	}

	public void setActual_price(String actual_price) {
		this.actual_price = actual_price;
	}

}
