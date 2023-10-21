package com.example.oc_p7_go4lunch;

import com.google.gson.annotations.SerializedName;

public class RestoInformations {
  private boolean isLiked;

  @SerializedName("formatted_phone_number")
  public String formattedPhoneNumber;

  @SerializedName("website")
  public String website;

  @SerializedName("likes")
  public String likes;

  public String getFormattedPhoneNumber() {
    return formattedPhoneNumber;
  }

  public void setFormattedPhoneNumber(String formattedPhoneNumber) {
    this.formattedPhoneNumber = formattedPhoneNumber;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getLikes() {
    return likes;
  }

  public void setLikes(String likes) {
    this.likes = likes;
  }


  public boolean isLiked() {
    return isLiked;
  }


  public void setLiked(boolean liked) {
    this.isLiked = liked;
  }
}
