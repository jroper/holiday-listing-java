package com.example.reservation.api;

import lombok.Value;

import java.util.UUID;

@Value
public class ReservationAdded {
  UUID reservationId;
  UUID listingId;
  Reservation reservation;
}
