package com.example.oc_p7_go4lunch.googleplaces;

import com.google.gson.annotations.SerializedName;

public class RestoInformations {
  private final boolean isLiked;

  @SerializedName("formatted_phone_number")
  public String formattedPhoneNumber;

  @SerializedName("website")
  public String website;

  public RestoInformations(boolean isLiked) {
    this.isLiked = isLiked;
  }

  public String getFormattedPhoneNumber() {
    return formattedPhoneNumber;
  }

  public String getWebsite() {
    return website;
  }

  public boolean isLiked() {
    return isLiked;
  }


}
