package com.example.reservation.api;

import lombok.Value;

import java.time.LocalDate;

@Value
public class Reservation {
  LocalDate checkin;
  LocalDate checkout;

  public boolean conflictsWith(Reservation other) {
    if (checkout.isBefore(other.checkin) || checkout.equals(other.checkin)) {
      return false;
    } else if (checkin.isAfter(other.checkout) || checkin.equals(other.checkout)) {
      return false;
    } else {
      return true;
    }
  }
}
