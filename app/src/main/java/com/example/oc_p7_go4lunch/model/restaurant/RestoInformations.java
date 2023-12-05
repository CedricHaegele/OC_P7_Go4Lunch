package com.example.oc_p7_go4lunch.model.restaurant;

import com.google.gson.annotations.SerializedName;

public class RestoInformations {
  @SerializedName("name")
  private String name;
  @SerializedName("website")
  private String website;
  @SerializedName("international_phone_number")
  private String phoneNumber;

  public String getName() {
    return name;
  }

  public String getWebsite() {
    return website;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

}
