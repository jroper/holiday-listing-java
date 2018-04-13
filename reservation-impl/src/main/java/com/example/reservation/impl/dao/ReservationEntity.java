package com.example.reservation.impl.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@NamedQueries({
    @NamedQuery(name = "hasConflicts",
        query = "select count(r) from ReservationEntity r where r.listingId = :listingId and r.checkout > :checkin and r.checkin < :checkout"),
    @NamedQuery(name = "currentReservations", query = "select r from ReservationEntity r where r.listingId  = :listingId and r.checkout >= :now order by r.checkin")
})
@Data
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class ReservationEntity {
  @Id
  UUID id;
  UUID listingId;
  LocalDate checkin;
  LocalDate checkout;
}
